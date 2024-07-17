/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { OpenSearchDomainStack } from "../lib/stacks/opensearch";
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import Project from "../lib/enums/project";
import { VpcStack } from "../lib/stacks/vpc";

test('OpenSearchDomain Stack Test', () => {
    const app = new App();
    const openSearchDomainStack = new OpenSearchDomainStack(app, 'OpenSearchHealth-OpenSearch', {
        region: "us-east-1",
        account: "test-account",
        vpcStack: new VpcStack(app, 'OpenSearchHealth-VPC', {}),
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles: [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const openSearchDomainStackTemplate = Template.fromStack(openSearchDomainStack);
    openSearchDomainStackTemplate.resourceCountIs('AWS::IAM::Role', 8);
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPool', 1);
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPoolGroup', 1);
    openSearchDomainStackTemplate.resourceCountIs('AWS::IAM::Policy', 4);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::IAM::Role', {
        "AssumeRolePolicyDocument": {
            "Statement": [
                {
                    "Action": "sts:AssumeRoleWithWebIdentity",
                    "Condition": {
                        "StringEquals": {
                            "cognito-identity.amazonaws.com:aud": {
                                "Ref": "IdentityPool"
                            }
                        },
                        "ForAnyValue:StringLike": {
                            "cognito-identity.amazonaws.com:amr": "authenticated"
                        }
                    },
                    "Effect": "Allow",
                    "Principal": {
                        "Federated": "cognito-identity.amazonaws.com"
                    }
                }
            ],
            "Version": "2012-10-17"
        }
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPoolGroup', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Cognito::UserPoolGroup', {
        "GroupName": "opensearch-admin-group",
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchHealthCognitoCognitoopensearchhealthidentitypoolAdminRole3AC37B11",
                "Arn"
            ]
        },
        "UserPoolId": {
            "Ref": "UserPool"
        }
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::IdentityPoolRoleAttachment', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Cognito::IdentityPoolRoleAttachment', {
        "IdentityPoolId": {
            "Ref": "IdentityPool"
        },
        "Roles": {
            "authenticated": {
                "Fn::GetAtt": [
                    "OpenSearchHealthCognitoCognitoopensearchhealthidentitypoolAuthRole50009EF2",
                    "Arn"
                ]
            }
        }
    });

    openSearchDomainStackTemplate.resourceCountIs('AWS::OpenSearchService::Domain', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::OpenSearchService::Domain', {
        "CognitoOptions": {
            "Enabled": true,
            "IdentityPoolId": {
                "Ref": "IdentityPool"
            },
            "RoleArn": {
                "Fn::GetAtt": [
                    "OpenSearchHealthCognitoAmazonOpenSearchServiceCognitoAccessA34D822B",
                    "Arn"
                ]
            },
            "UserPoolId": {
                "Ref": "UserPool"
            }
        },
        "ClusterConfig": {
            "DedicatedMasterCount": 3,
            "DedicatedMasterEnabled": true,
            "DedicatedMasterType": "m6g.xlarge.search",
            "InstanceCount": 6,
            "InstanceType": "r6g.2xlarge.search",
            "ZoneAwarenessConfig": {
                "AvailabilityZoneCount": 2
            },
            "ZoneAwarenessEnabled": true
        },
        "EngineVersion": "OpenSearch_2.13",
    });

    openSearchDomainStackTemplate.resourceCountIs('AWS::Route53::RecordSet', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Route53::RecordSet', {
        "Name": `${Project.METRICS_COGNITO_HOSTED_ZONE}.`,
        "Type": "A"
    });

    openSearchDomainStackTemplate.resourceCountIs('AWS::AutoScaling::LaunchConfiguration', 1);
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
