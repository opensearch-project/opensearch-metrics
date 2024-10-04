/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import Project from "../lib/enums/project";
import { VpcStack } from "../lib/stacks/vpc";
import { GitHubAutomationApp } from "../lib/stacks/gitHubAutomationApp";
import { OpenSearchMetricsSecretsStack } from "../lib/stacks/secrets";
import {GitHubWorkflowMonitorAlarms} from "../lib/stacks/gitHubWorkflowMonitorAlarms";


test('OpenSearch GitHub App Stack test ', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, "Test-OpenSearchHealth-VPC", {});
    const openSearchMetricsGitHubAppSecretStack = new OpenSearchMetricsSecretsStack(app, "Test-OpenSearchMetrics-GitHubAutomationApp-Secret", {
        secretName: 'test-github-app-creds'
    });

    const gitHubWorkflowMonitorAlarms = new GitHubWorkflowMonitorAlarms(app, "Test-OpenSearchMetrics-GitHubWorkflowMonitor-Alarms", {
        namespace: 'GitHubActions',
        metricName: 'WorkflowRunFailures',
        workflows: [
            'Publish snapshots to maven',
            'Run performance benchmark on pull request',
        ],
    });
    const gitHubApp = new GitHubAutomationApp(app, "Test-OpenSearchMetrics-GitHubAutomationApp", {
        vpc: vpcStack.vpc,
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        ami: Project.EC2_AMI_SSM.toString(),
        secret: openSearchMetricsGitHubAppSecretStack.secret,
        workflowAlarmsArn: gitHubWorkflowMonitorAlarms.workflowAlarmsArn
    });

    const template = Template.fromStack(gitHubApp);
    template.resourceCountIs('AWS::IAM::Role', 1);
    template.hasResourceProperties('AWS::IAM::Role', {
        AssumeRolePolicyDocument: {
            Statement: Match.arrayWith([
                {
                    Action: "sts:AssumeRole",
                    Effect: "Allow",
                    Principal: {
                        Service: "ec2.amazonaws.com"
                    }
                }
            ])
        }
    });
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
        GroupDescription: Match.stringLikeRegexp('Test-OpenSearchMetrics-GitHubAutomationApp/OpenSearchMetrics-GitHubAutomationApp-Asg/InstanceSecurityGroup')
    });
    template.resourceCountIs('AWS::IAM::Policy', 1);
    template.hasResourceProperties('AWS::IAM::Policy', {
        PolicyDocument: {
            Statement: Match.arrayWith([
                Match.objectLike({
                    Action: "secretsmanager:GetSecretValue",
                    Effect: "Allow",
                    Resource: {
                        "Fn::ImportValue": Match.stringLikeRegexp('Test-OpenSearchMetrics-GitHubAutomationApp-Secret:ExportsOutputRefMetricsCreds.*')
                    }
                })
            ])
        }
    });
    template.hasResourceProperties('AWS::AutoScaling::AutoScalingGroup', {
        DesiredCapacity: "1",
        MaxSize: "1",
        MinSize: "1",
    })

    // IAM Policy Test
    template.resourceCountIs('AWS::IAM::Policy', 1);
    template.hasResourceProperties('AWS::IAM::Policy', {
        PolicyDocument: {
            Statement: Match.arrayWith([
                Match.objectLike({
                    Action: "cloudwatch:PutMetricData",
                    Effect: "Allow",
                    Resource: "*"
                })
            ])
        }
    });
});
