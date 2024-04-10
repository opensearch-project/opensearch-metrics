import {App} from "aws-cdk-lib";
import {VpcStack} from "../lib/stacks/vpc";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchMetricsNginxReadonly} from "../lib/stacks/opensearchNginxProxyReadonly";
import Project from "../lib/enums/project";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchHealthRoute53} from "../lib/stacks/route53";

test('OpenSearchMetricsNginxReadonly Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        vpcStack: vpcStack,
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles:  [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const metricsHostedZone = new OpenSearchHealthRoute53(app, "OpenSearchMetrics-HostedZone", {
        hostedZone: Project.METRICS_HOSTED_ZONE,
        appName: "OpenSearchMetrics"
    });
    const stack = new OpenSearchMetricsNginxReadonly(app, 'Test-OpenSearchMetricsNginxReadonly', {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        vpc: vpcStack.vpc,
        securityGroup: vpcStack.securityGroup,
        opensearchDashboardUrlProps: {
            opensearchDashboardVpcUrl: openSearchDomainStack.domain.domainEndpoint,
            openSearchDomainName: openSearchDomainStack.domain.domainName
        },
        albProps: {
            hostedZone: metricsHostedZone,
            certificateArn: metricsHostedZone.certificateArn,
        },
    });
    const template = Template.fromStack(stack);
    template.resourceCountIs('AWS::Route53::RecordSet', 1);
    template.hasResourceProperties('AWS::Route53::RecordSet', {
        "Name": "metrics.opensearch.org.",
        "Type": "A"
    });
    template.resourceCountIs('AWS::EC2::SecurityGroup', 2);
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupEgress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow all outbound traffic by default",
                "IpProtocol": "-1"
            }
        ]
    });
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupIngress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow from anyone on port 443",
                "FromPort": 443,
                "IpProtocol": "tcp",
                "ToPort": 443
            }
        ]
    });
});

test('OpenSearchMetricsNginxCognito Test', () => {
    const app = new App();
    const openSearchDomainStack = new OpenSearchDomainStack(app, 'Test-OpenSearchHealth-OpenSearch', {
        region: "us-east-1",
        account: "test-account",
        vpcStack: new VpcStack(app, 'OpenSearchHealth-VPC', {}),
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles:  [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const openSearchDomainStackTemplate = Template.fromStack(openSearchDomainStack);
    openSearchDomainStackTemplate.resourceCountIs('AWS::Route53::RecordSet', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Route53::RecordSet', {
        "Name": "metrics.login.opensearch.org.",
        "Type": "A"
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::EC2::SecurityGroup', 2);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupEgress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow all outbound traffic by default",
                "IpProtocol": "-1"
            }
        ]
    });
    openSearchDomainStackTemplate.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupIngress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow from anyone on port 443",
                "FromPort": 443,
                "IpProtocol": "tcp",
                "ToPort": 443
            }
        ]
    });
    openSearchDomainStackTemplate.hasResourceProperties('AWS::IAM::Role', {
            "AssumeRolePolicyDocument": {
                "Statement": [
                    {
                        "Action": "sts:AssumeRole",
                        "Effect": "Allow",
                        "Principal": {
                            "Service": "ec2.amazonaws.com"
                        }
                    }
                ],
                "Version": "2012-10-17"
            },
            "ManagedPolicyArns": [
                {
                    "Fn::Join": [
                        "",
                        [
                            "arn:",
                            {
                                "Ref": "AWS::Partition"
                            },
                            ":iam::aws:policy/AmazonSSMManagedInstanceCore"
                        ]
                    ]
                }
            ],
            "RoleName": "OpenSearchCognitoUserAccess"
    })
    openSearchDomainStackTemplate.resourceCountIs('AWS::ElasticLoadBalancingV2::LoadBalancer', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::ElasticLoadBalancingV2::LoadBalancer', {
        "LoadBalancerAttributes": [
            {
                "Key": "deletion_protection.enabled",
                "Value": "false"
            }
        ],
        "Scheme": "internet-facing",
        "Type": "application"
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::ElasticLoadBalancingV2::Listener', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::ElasticLoadBalancingV2::Listener', {
        "Port": 443,
        "Protocol": "HTTPS"
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::ElasticLoadBalancingV2::TargetGroup', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::ElasticLoadBalancingV2::TargetGroup', {
        "HealthCheckPath": "/",
        "HealthCheckPort": "80",
        "Port": 443,
        "Protocol": "HTTPS",
        "TargetGroupAttributes": [
            {
                "Key": "stickiness.enabled",
                "Value": "false"
            }
        ],
        "TargetType": "instance",
        "VpcId": {
            "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcB885AABED860B3EB"
        }
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::AutoScaling::AutoScalingGroup', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::AutoScaling::AutoScalingGroup', {
            "DesiredCapacity": "1",
            "HealthCheckGracePeriod": 90,
            "HealthCheckType": "EC2",
            "LaunchConfigurationName": {
                "Ref": "OpenSearchMetricsNginxOpenSearchMetricsCognitoMetricsProxyAsgLaunchConfig8D060946"
            },
            "MaxSize": "1",
            "MinSize": "1",
            "Tags": [
                {
                    "Key": "name",
                    "PropagateAtLaunch": true,
                    "Value": "OpenSearchMetricsCognito-NginxProxyHost"
                },
                {
                    "Key": "Name",
                    "PropagateAtLaunch": true,
                    "Value": "OpenSearchMetricsCognito"
                }
            ],
            "TargetGroupARNs": [
                {
                    "Ref": "OpenSearchMetricsNginxOpenSearchMetricsCognitoNginxProxyAlbOpenSearchMetricsCognitoNginxProxyAlbListenerOpenSearchMetricsCognitoNginxProxyAlbTargetGroup8E449B4A"
                }
            ],
            "VPCZoneIdentifier": [
                {
                    "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcPrivateSubnet1Subnet529349B600974078"
                },
                {
                    "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcPrivateSubnet2SubnetBA599EDB2BEEEA30"
                }
            ]
    });

});
