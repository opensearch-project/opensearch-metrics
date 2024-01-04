import {
    AmazonLinuxGeneration,
    AmazonLinuxImage,
    InstanceClass,
    InstanceSize,
    InstanceType, Peer, Port,
    SecurityGroup, SubnetType,
    Vpc
} from "aws-cdk-lib/aws-ec2";
import {Construct} from "constructs";
import {
    AutoScalingGroup,
    BlockDeviceVolume,
    CfnLaunchConfiguration,
    HealthCheck,
    UpdatePolicy
} from "aws-cdk-lib/aws-autoscaling";
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from "aws-cdk-lib/aws-iam";
import {Aspects, Duration, Stack, Tag} from "aws-cdk-lib";
import {ListenerCertificate, NetworkLoadBalancer, Protocol} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {ARecord, RecordTarget} from "aws-cdk-lib/aws-route53";
import Project from "../enums/project";
import {LoadBalancerTarget} from "aws-cdk-lib/aws-route53-targets";
import {OpenSearchHealthRoute53} from "./route53";

export interface ServerProps {
    readonly vpc: Vpc;
    readonly securityGroup: SecurityGroup;
    readonly opensearchUrl: string;
    readonly nlbProps?: nlbProps
    readonly region: string;
}

export interface nlbProps {
    hostedZone: OpenSearchHealthRoute53;
    certificateArn: string,
}

export class OpenSearchHealthFrontendServer extends Stack {
    readonly asg: AutoScalingGroup;

    constructor(scope: Construct, id: string, props: ServerProps) {
        const {vpc, securityGroup} = props;

        super(scope, id);

        const instanceRole = this.createInstanceRole();
        this.asg = new AutoScalingGroup(this, 'OpenSearchHealth-FrontendServer', {
            instanceType: InstanceType.of(InstanceClass.M5, InstanceSize.LARGE),
            blockDevices: [{ deviceName: '/dev/xvda', volume: BlockDeviceVolume.ebs(10) }], // GB
            healthCheck: HealthCheck.ec2({ grace: Duration.seconds(90) }),
            machineImage: new AmazonLinuxImage({
                generation: AmazonLinuxGeneration.AMAZON_LINUX_2,
            }),
            // Temp added private subnet
            // associatePublicIpAddress: true,
            associatePublicIpAddress: false,
            allowAllOutbound: true,
            desiredCapacity: 1,
            minCapacity: 1,
            vpc: props.vpc,
            vpcSubnets: {
                // subnetType: SubnetType.PUBLIC,
                subnetType: SubnetType.PRIVATE_WITH_EGRESS
            },
            role: instanceRole,
            updatePolicy: UpdatePolicy.replacingUpdate()
        });
        this.asg.addSecurityGroup(props.securityGroup);

        // Allow traffic from the VPC
        this.asg.connections.allowFrom(Peer.ipv4(props.vpc.vpcCidrBlock), Port.allTcp(), 'Local VPC Access');

        if (props.nlbProps) {
            const lb = new NetworkLoadBalancer(this, `OpenSearchHealth-FrontendNlb`, {
                loadBalancerName: "OpenSearchHealth-FrontendNlb",
                vpc: vpc,
                internetFacing: true
            });

            const listenerCertificate = ListenerCertificate.fromArn(props.nlbProps.certificateArn);

            const listener = lb.addListener(`OpenSearchHealth-FrontendNlbListener`, {
                port: 443,
                protocol: Protocol.TLS,
                certificates: [listenerCertificate]
            });

            listener.addTargets(`OpenSearchHealth-FrontendNlbTarget`, {
                port: 443,
                targets: [this.asg]
            });

            const aRecord = new ARecord(this, "OpenSearchHealth-DNS", {
                zone: props.nlbProps.hostedZone.zone,
                recordName: Project.HOSTED_ZONE,
                target: RecordTarget.fromAlias(new LoadBalancerTarget(lb)),
            });
        }

        // Enforces IMDSv2
        const launchConfiguration = this.asg.node.findChild('LaunchConfig') as CfnLaunchConfiguration;
        launchConfiguration.metadataOptions = {
            httpPutResponseHopLimit: 2,
            httpEndpoint: "enabled",
            httpTokens: "required"
        };

        this.asg.addSecurityGroup(securityGroup);
        // Allow traffic from the VPC
        this.asg.connections.allowFrom(
            Peer.ipv4(vpc.vpcCidrBlock),
            Port.allTcp(),
            "Local VPC Access"
        );

        this.asg.connections.allowFrom(
            Peer.prefixList(Project.RESTRICTED_PREFIX),
            Port.tcp(443),
            "Allow All"
        );

        const instanceName = `OpenSearchHealth-FrontendServer`;
        Aspects.of(this.asg).add(new Tag('name', instanceName, {
            applyToLaunchedInstances: true,
            includeResourceTypes: ['AWS::AutoScaling::AutoScalingGroup']
        }),);

        // Commands run on provisioning
        this.asg.addUserData(...this.getUserData(props.opensearchUrl, props.region, "opensearch_repo_health", "opensearch_project_health"));
    }

    private createInstanceRole(): Role {
        const role = new Role(this, `OpenSearchHealth-FrontendServerRole`, {
            assumedBy: new ServicePrincipal('ec2.amazonaws.com'),
            roleName: "OpenSearchFrontEndServer",
        });
        // SSM integration - https://aws.amazon.com/systems-manager/
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'));
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonOpenSearchServiceReadOnlyAccess'));
        // Temp access until the GitHub repo is public, to fetch the code from s3
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonS3FullAccess'));

        role.addToPolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'ssm:GetCommandInvocation'
            ],
            resources: [
                '*'
            ]
        }));

        role.addToPolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                'es:ESHttpPost'
            ],
            resources: [
                '*'
            ]
        }));


        return role;
    }
    private buildOpenSearchDashboardConf(): string {
        return `'# See for reference template for opensearchdashboard:
            resolver 10.0.0.2 ipv6=off;
            
            server {
                listen 443;
                server_name $host;
            
                ssl_certificate /etc/nginx/cert.crt;
                ssl_certificate_key /etc/nginx/cert.key;
            
                ssl on;
                ssl_session_cache builtin:1000 shared:SSL:10m;
                ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
                ssl_ciphers HIGH:!aNULL:!eNULL:!EXPORT:!CAMELLIA:!DES:!MD5:!PSK:!RC4;
                ssl_prefer_server_ciphers on;
            
                set $frontend_endpoint localhost:8080;
                proxy_cookie_domain $frontend_endpoint $host;
            
                location / {
                    # Forward requests to Dashboards
                    proxy_pass http://$frontend_endpoint;
                    # Response buffer settings
                    proxy_buffer_size 128k;
                    proxy_buffers 4 256k;
                    proxy_busy_buffers_size 256k;
                }
            }'`;
    }
    private getUserData(opensearchUrl: string, region: string, repoIndex: string, projectIndex: string): string[] {
        return [
            'sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm',
            'sudo amazon-linux-extras install nginx1.12 -y',
            'sudo openssl req -x509 -nodes -newkey rsa:4096 -keyout /etc/nginx/cert.key -out /etc/nginx/cert.crt -days 365 -subj \'/CN=SH\'',
            'sudo echo ' + this.buildOpenSearchDashboardConf() + ' > /etc/nginx/conf.d/frontendserver.conf',
            'sudo systemctl start nginx',
            'sudo systemctl enable nginx',
            'sudo amazon-linux-extras install docker -y',
            'sudo systemctl enable docker',
            'sudo systemctl start docker',
            'sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/sbin/docker-compose',
            'sudo chmod a+x /usr/local/sbin/docker-compose',
            'sudo aws s3 sync ' +  Project.FRONTEND_CODE_S3_LOCATION + ' opensearch-health-dashboard',
            'cd opensearch-health-dashboard',
            'DOMAIN_ENDPOINT=https://' + opensearchUrl + ' DOMAIN_REGION=' + region + ' HEALTH_INDEX=' + repoIndex + ' PROJECT_INDEX=' + projectIndex + ' ENABLE_SIGV4=true' + ' docker-compose up -d'
        ];
    }
}