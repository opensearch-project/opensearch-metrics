/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import Project from "../lib/enums/project";
import { OpenSearchDomainStack } from "../lib/stacks/opensearch";
import { OpenSearchMetricsNginxReadonly } from "../lib/stacks/opensearchNginxProxyReadonly";
import { OpenSearchHealthRoute53 } from "../lib/stacks/route53";
import { OpenSearchS3 } from "../lib/stacks/s3";
import { VpcStack } from "../lib/stacks/vpc";

test('OpenSearchMetricsNginxReadonly Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});
    const s3Stack = new OpenSearchS3(app, "Test-OpenSearchMetrics-GitHubAutomationAppEvents-S3");
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        vpcStack: vpcStack,
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles: [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        },
        githubEventsBucket: s3Stack.bucket
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
                "Description": "Allow HTTPS 443 Access",
                "FromPort": 443,
                "IpProtocol": "tcp",
                "ToPort": 443
            }
        ]
    });
    template.resourceCountIs('AWS::AutoScaling::LaunchConfiguration', 1);
    template.hasResourceProperties('AWS::AutoScaling::LaunchConfiguration', {
        "InstanceType": "m5.xlarge",
        "BlockDeviceMappings": [
            {
                "DeviceName": "/dev/xvda",
                "Ebs": {
                    "VolumeSize": 50
                }
            }
        ],
    });
    template.resourceCountIs('AWS::IAM::Policy', 1);
    template.hasResourceProperties('AWS::IAM::Policy', {
        "PolicyDocument": {
            "Statement": [
                {
                    "Action": [
                        "es:Describe*",
                        "es:List*",
                        "es:Get*",
                        "es:ESHttpGet",
                        "es:ESHttpPost",
                    ],
                    "Effect": "Allow",
                    "Resource": {
                        "Fn::Join": [
                            "",
                            [
                                "arn:aws:es:::domain/",
                                {
                                    "Fn::ImportValue": "OpenSearchHealth-OpenSearch:ExportsOutputRefOpenSearchHealthDomainD942887BFEBF5289"
                                },
                                "/*"
                            ]
                        ]
                    }
                },
                {
                    "Action": [
                        "logs:CreateLogGroup",
                        "logs:CreateLogStream",
                        "logs:PutLogEvents",
                        "logs:DescribeLogStreams"
                    ],
                    "Effect": "Allow",
                    "Resource": "arn:aws:logs:::log-group:OpenSearchMetrics/aws-sigv4-proxy.log:*"
                }
            ],
            "Version": "2012-10-17"
        },
        "PolicyName": "OpenSearchMetricsReadonlyNginxProxyRoleDefaultPolicy8EDC749D",
        "Roles": [
            {
                "Ref": "OpenSearchMetricsReadonlyNginxProxyRoleE26CC937"
            }
        ]
    });
});

