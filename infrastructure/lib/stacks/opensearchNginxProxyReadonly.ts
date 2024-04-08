import { BlockDeviceVolume, CfnLaunchConfiguration, HealthCheck, UpdatePolicy, AutoScalingGroup } from 'aws-cdk-lib/aws-autoscaling';
import {
    InstanceClass,
    InstanceSize,
    InstanceType,
    Peer,
    Port,
    SecurityGroup,
    SubnetType,
    Vpc,
    AmazonLinuxGeneration,
    AmazonLinuxImage, MachineImage
} from 'aws-cdk-lib/aws-ec2';
import * as iam from "aws-cdk-lib/aws-iam";
import {Aspects, CfnOutput, Duration, Stack, Tag, Tags} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import {
    ApplicationLoadBalancer, ApplicationProtocol,
    ListenerCertificate, SslPolicy,
} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import Project from "../enums/project";
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchHealthRoute53} from "./route53";
import {ARecord, RecordTarget} from "aws-cdk-lib/aws-route53";
import {LoadBalancerTarget} from "aws-cdk-lib/aws-route53-targets";
import {OpenSearchWAF} from "./waf";


export interface NginxProps {
    readonly vpc: Vpc;
    readonly securityGroup: SecurityGroup;
    readonly opensearchDashboardUrlProps: opensearchDashboardUrlProps;
    readonly albProps?: albProps
    readonly region: string;
    readonly account: string;
}

export interface albProps {
    hostedZone: OpenSearchHealthRoute53;
    certificateArn: string,
}

export interface opensearchDashboardUrlProps {
    opensearchDashboardVpcUrl: string,
    openSearchDomainName: string,
}

export class OpenSearchMetricsNginxReadonly extends Stack {

    static readonly READONLY_ALB_ARN: string = 'readOnlyAlbArn';

    readonly asg: AutoScalingGroup;

    constructor(scope: Construct, id: string, props: NginxProps) {
        const { vpc, securityGroup } = props;

        super(scope, id);

        const instanceRole = this.createNginxReadonlyInstanceRole(props);
            this.asg = new AutoScalingGroup(this, 'OpenSearchMetricsReadonly-MetricsProxyAsg', {
            instanceType: InstanceType.of(InstanceClass.M5, InstanceSize.LARGE),
            blockDevices: [{ deviceName: '/dev/xvda', volume: BlockDeviceVolume.ebs(10) }], // GB
            healthCheck: HealthCheck.ec2({ grace: Duration.seconds(90) }),
            machineImage: MachineImage.latestAmazonLinux2(),
            associatePublicIpAddress: false,
            allowAllOutbound: true,
            desiredCapacity: 2,
            minCapacity: 2,
            vpc: props.vpc,
            vpcSubnets: {
                subnetType: SubnetType.PRIVATE_WITH_EGRESS
            },
            role: instanceRole,
            updatePolicy: UpdatePolicy.replacingUpdate()
        });
        Tags.of(this.asg).add("Name", "OpenSearchMetricsReadonly")

        if (props.albProps) {
            const albSecurityGroup = new SecurityGroup(this, 'ALBSecurityGroup', {
                vpc,
                allowAllOutbound: true,
            });
            albSecurityGroup.addIngressRule(Peer.prefixList(Project.RESTRICTED_PREFIX), Port.tcp(443));

            const openSearchApplicationLoadBalancer = new ApplicationLoadBalancer(this, 'OpenSearchMetricsReadonly-NginxProxyAlb', {
                loadBalancerName: "OpenSearchMetricsReadonly",
                vpc: vpc,
                internetFacing: true,
                securityGroup: albSecurityGroup
            });

            new CfnOutput(this, 'readOnlyAlbArn', {
                value: openSearchApplicationLoadBalancer.loadBalancerArn,
                exportName: OpenSearchMetricsNginxReadonly.READONLY_ALB_ARN,
            });

            //const importedArnSecretBucketValue = Fn.importValue(`${CIConfigStack.CERTIFICATE_ARN_SECRET_EXPORT_VALUE}`);


            const listenerCertificate = ListenerCertificate.fromArn(props.albProps.certificateArn);

            const listener = openSearchApplicationLoadBalancer.addListener(`OpenSearchMetricsReadonly-NginxProxyAlbListener`, {
                port: 443,
                protocol: ApplicationProtocol.HTTPS,
                sslPolicy: SslPolicy.RECOMMENDED_TLS,
                certificates: [listenerCertificate]
            });

            listener.addTargets(`OpenSearchMetricsReadonly-NginxProxyAlbTarget`, {
                port: 443,
                protocol: ApplicationProtocol.HTTPS,
                healthCheck: {
                    port: '80',
                    path: '/',
                },
                targets: [this.asg]
            });

            const aRecord = new ARecord(this, "OpenSearchMetricsReadonly-DNS", {
                zone: props.albProps.hostedZone.zone,
                recordName: Project.METRICS_HOSTED_ZONE,
                target: RecordTarget.fromAlias(new LoadBalancerTarget(openSearchApplicationLoadBalancer)),
            });
        }


        const launchConfiguration = this.asg.node.findChild('LaunchConfig') as CfnLaunchConfiguration;
        launchConfiguration.metadataOptions = {
            httpPutResponseHopLimit: 2,
            httpEndpoint: "enabled",
            httpTokens: "required"
        };

        const instanceName = `OpenSearchMetricsReadonly-NginxProxyHost`;
        Aspects.of(this.asg).add(new Tag('name', instanceName, {
            applyToLaunchedInstances: true,
            includeResourceTypes: ['AWS::AutoScaling::AutoScalingGroup']
        }),);

        this.asg.addUserData(...this.getUserData(props));
    }



    private buildOpenSearchDashboardConf(nginxProps: NginxProps): string {
        return `'# See for reference template for opensearchdashboard:
            resolver 10.0.0.2 ipv6=off;
            
            server {
                listen 443;
                server_name $host;
                rewrite ^/$ https://$host/_dashboards redirect;
            
                ssl_certificate /etc/nginx/cert.crt;
                ssl_certificate_key /etc/nginx/cert.key;
            
                ssl on;
                ssl_session_cache builtin:1000 shared:SSL:10m;
                ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
                ssl_ciphers HIGH:!aNULL:!eNULL:!EXPORT:!CAMELLIA:!DES:!MD5:!PSK:!RC4;
                ssl_prefer_server_ciphers on;
            
                set $os_endpoint ${nginxProps.opensearchDashboardUrlProps.opensearchDashboardVpcUrl};
                set $frontend_endpoint localhost:8081;
                proxy_cookie_domain $frontend_endpoint $host;
            
                location ^~ /_dashboards {
                    proxy_pass http://$frontend_endpoint;
                    proxy_set_header Host $os_endpoint;  # Set the Host header to the original value
                    proxy_set_header X-Real-IP $remote_addr;  # Set the X-Real-IP header
                    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;  # Set the X-Forwarded-For header
                    proxy_set_header X-Forwarded-Proto $scheme;  # Set the X-Forwarded-Proto header
                }
            }'`;
    }

    private getUserData(nginxProps: NginxProps): string[] {
        return [
            'sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm',
            'sudo amazon-linux-extras install nginx1.12 -y',
            'sudo openssl req -x509 -nodes -newkey rsa:4096 -keyout /etc/nginx/cert.key -out /etc/nginx/cert.crt -days 365 -subj \'/CN=SH\'',
            'sudo echo ' + this.buildOpenSearchDashboardConf(nginxProps) + ' > /etc/nginx/conf.d/opensearchdashboard.conf',
            'sudo systemctl start nginx',
            'sudo systemctl enable nginx',
            'sudo amazon-linux-extras install docker -y',
            'sudo systemctl enable docker',
            'sudo systemctl start docker',
            'docker run --rm -tid -v ~/.aws:/root/.aws -p 8081:8080 public.ecr.aws/aws-observability/aws-sigv4-proxy:1.8 -v --name es --region us-east-1'
        ];
    }

    private createNginxReadonlyInstanceRole(nginxProps: NginxProps): Role {
        const role = new Role(this, `OpenSearchMetricsReadonly-NginxProxyRole`, {
            assumedBy: new ServicePrincipal('ec2.amazonaws.com'),
            roleName: "OpenSearchReadonlyUserAccess",
        });
        const domainArn = `arn:aws:es:${nginxProps.region}:${nginxProps.account}:domain/${nginxProps.opensearchDashboardUrlProps.openSearchDomainName}/*`;
        // SSM integration - https://aws.amazon.com/systems-manager/
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'));

        role.addToPolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                "es:Describe*",
                "es:List*",
                "es:Get*",
                "es:ESHttpGet",
                "es:ESHttpPost"
            ],
            resources: [domainArn]
        }));
        return role;
    }
}
