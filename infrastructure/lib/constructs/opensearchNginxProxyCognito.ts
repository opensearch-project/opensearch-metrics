import {
    BlockDeviceVolume,
    HealthCheck,
    UpdatePolicy,
    AutoScalingGroup,
    CfnLaunchConfiguration
} from 'aws-cdk-lib/aws-autoscaling';
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
import { Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal } from 'aws-cdk-lib/aws-iam';
import {Aspects, CfnOutput, Duration, Tag, Tags} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import {
    ApplicationLoadBalancer, ApplicationProtocol,
    ListenerCertificate,
} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import Project from "../enums/project";
import {ARecord, RecordTarget} from "aws-cdk-lib/aws-route53";
import {LoadBalancerTarget} from "aws-cdk-lib/aws-route53-targets";
import {OpenSearchHealthRoute53} from "../stacks/route53";


export interface NginxProps {
    readonly vpc: Vpc;
    readonly securityGroup: SecurityGroup;
    readonly opensearchDashboardUrlProps: opensearchDashboardUrlProps;
    readonly albProps?: albProps
    readonly region: string;
}

export interface albProps {
    hostedZone: OpenSearchHealthRoute53;
    certificateArn: string,
}

export interface opensearchDashboardUrlProps {
    opensearchDashboardVpcUrl: string,
    cognitoDomain: string
}

export class OpenSearchMetricsNginxCognito extends Construct {
    readonly asg: AutoScalingGroup;

    constructor(scope: Construct, id: string, props: NginxProps) {
        const { vpc, securityGroup, opensearchDashboardUrlProps } = props;

        super(scope, id);

        const instanceRole = this.createInstanceRole();

        this.asg = new AutoScalingGroup(this, 'OpenSearchMetricsCognito-MetricsProxyAsg', {
            instanceType: InstanceType.of(InstanceClass.M5, InstanceSize.LARGE),
            blockDevices: [{ deviceName: '/dev/xvda', volume: BlockDeviceVolume.ebs(10) }], // GB
            healthCheck: HealthCheck.ec2({ grace: Duration.seconds(90) }),
            machineImage: MachineImage.latestAmazonLinux2(),
            // Temp added public subnet and IP, until backed up by ALB
            associatePublicIpAddress: true,
            allowAllOutbound: true,
            desiredCapacity: 1,
            minCapacity: 1,
            vpc: props.vpc,
            vpcSubnets: {
                // Temp added public subnet and IP, until backed up by ALB
               // subnetType: SubnetType.PUBLIC,
                subnetType: SubnetType.PUBLIC
            },
            role: instanceRole,
            // Actually update the existing instance instead of leaving it running.  This will build a new ASG
            // and then destroy the old one in order to maintain availability
            updatePolicy: UpdatePolicy.replacingUpdate()
        });
        Tags.of(this.asg).add("Name", "OpenSearchMetricsCognito")

        if (props.albProps) {
            const openSearchCognitoApplicationLoadBalancer = new ApplicationLoadBalancer(this, `OpenSearchMetricsCognito-NginxProxyAlb`, {
                loadBalancerName: "OpenSearchMetricsCognito",
                vpc: vpc,
                internetFacing: true
            });

            const listenerCertificate = ListenerCertificate.fromArn(props.albProps.certificateArn);

            const listener = openSearchCognitoApplicationLoadBalancer.addListener(`OpenSearchMetricsCognito-NginxProxyAlbListener`, {
                port: 443,
                protocol: ApplicationProtocol.HTTPS,
                certificates: [listenerCertificate]
            });

            listener.addTargets(`OpenSearchMetricsCognito-NginxProxyAlbTarget`, {
                port: 443,
                protocol: ApplicationProtocol.HTTPS,
                targets: [this.asg]
            });


            const aRecord = new ARecord(this, "OpenSearchMetricsCognito-DNS", {
                zone: props.albProps.hostedZone.zone,
                recordName: Project.METRICS_HOSTED_ZONE,
                target: RecordTarget.fromAlias(new LoadBalancerTarget(openSearchCognitoApplicationLoadBalancer)),
            });
        }

        const launchConfiguration = this.asg.node.findChild('LaunchConfig') as CfnLaunchConfiguration;
        launchConfiguration.metadataOptions = {
            httpPutResponseHopLimit: 2,
            httpEndpoint: "enabled",
            httpTokens: "required"
        };

        // To ensure the Cfn outputs are not deleted
        new CfnOutput(this, 'VpcCidr', {
            value: vpc.vpcCidrBlock,
            description: 'VPC CIDR',
        });

        this.asg.connections.allowFrom(
            Peer.prefixList(Project.RESTRICTED_PREFIX),
            Port.tcp(443),
            "Allow All"
        );

        const instanceName = `OpenSearchMetricsCognito-NginxProxyHost`;
        Aspects.of(this.asg).add(new Tag('name', instanceName, {
            applyToLaunchedInstances: true,
            includeResourceTypes: ['AWS::AutoScaling::AutoScalingGroup']
        }),);

        // Commands run on provisioning
        this.asg.addUserData(...this.getUserData(opensearchDashboardUrlProps, props.region));
    }

    private createInstanceRole(): Role {
        const role = new Role(this, `OpenSearchMetricsCognito-NginxProxyRole`, {
            assumedBy: new ServicePrincipal('ec2.amazonaws.com'),
            roleName: "OpenSearchCognitoUserAccess",
        });
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'));
        return role;
    }


    private buildOpenSearchDashboardConf(opensearchDashboardUrlProps: opensearchDashboardUrlProps, region: string): string {
        const cognitoUrl: string = opensearchDashboardUrlProps.cognitoDomain + '.auth.' + region + '.amazoncognito.com';
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
            
                set $os_endpoint ${opensearchDashboardUrlProps.opensearchDashboardVpcUrl};
                set $cognito_endpoint ${cognitoUrl};
            
                location ^~ /_dashboards {
                    # Forward requests to Dashboards
                    proxy_pass https://$os_endpoint;
            
                    # Handle redirects to Amazon Cognito
                    proxy_redirect https://$cognito_endpoint https://$host;
            
                    # Update cookie domain and path
                    proxy_cookie_domain $os_endpoint $host;
                    
     
                    # Response buffer settings
                    proxy_buffer_size 128k;
                    proxy_buffers 4 256k;
                    proxy_busy_buffers_size 256k;
                }
            
                location ~ \\/(log|sign|error|fav|forgot|change|saml|oauth2|confirm) {
                    # Forward requests to Cognito
                    proxy_pass https://$cognito_endpoint;
            
                    # Handle redirects to opensearchdashboard
                    proxy_redirect https://$os_endpoint https://$host;
            
                    # Handle redirects to Amazon Cognito
                    proxy_redirect https://$cognito_endpoint https://$host;
            
                    # Update cookie domain
                    proxy_cookie_domain $cognito_endpoint $host;
                }
            }'`;
    }

    private getUserData(opensearchDashboardUrlProps: opensearchDashboardUrlProps, region: string): string[] {
        return [
            'sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm',
            'sudo amazon-linux-extras install nginx1.12 -y',
            'sudo openssl req -x509 -nodes -newkey rsa:4096 -keyout /etc/nginx/cert.key -out /etc/nginx/cert.crt -days 365 -subj \'/CN=SH\'',
            'sudo echo ' + this.buildOpenSearchDashboardConf(opensearchDashboardUrlProps, region) + ' > /etc/nginx/conf.d/opensearchdashboard.conf',
            'sudo systemctl start nginx',
            'sudo systemctl enable nginx'
        ];
    }
}
