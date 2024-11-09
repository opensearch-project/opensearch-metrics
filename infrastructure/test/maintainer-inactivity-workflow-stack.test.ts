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
import { OpenSearchMetricsWorkflowStack } from "../lib/stacks/metricsWorkflow";
import Project from "../lib/enums/project";
import { OpenSearchDomainStack } from "../lib/stacks/opensearch";
import { VpcStack } from "../lib/stacks/vpc";
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import {OpenSearchS3} from "../lib/stacks/s3";
import {OpenSearchMaintainerInactivityWorkflowStack} from "../lib/stacks/maintainerInactivityWorkflow";

test('Maintainer Inactivity Workflow Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'Test-OpenSearchHealth-VPC', {});
    const s3Stack = new OpenSearchS3(app, "Test-OpenSearchMetrics-GitHubAutomationAppEvents-S3");
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
        },
        githubAutomationAppAccess: "sample-role-arn",
        githubEventsBucket: s3Stack.bucket,
    });
    const openSearchMaintainerInactivityWorkflowStack = new OpenSearchMaintainerInactivityWorkflowStack(app, 'Test-OpenSearchMaintainerInactivity-Workflow', {
        opensearchDomainStack: openSearchDomainStack,
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE,
    });
    const template = Template.fromStack(openSearchMaintainerInactivityWorkflowStack);
    template.resourceCountIs('AWS::IAM::Role', 2);
    template.resourceCountIs('AWS::Lambda::Function', 1);
    template.hasResourceProperties('AWS::Lambda::Function', {
        "FunctionName": "OpenSearchMetricsMaintainerInactivityLambda",
        "Handler": "org.opensearchmetrics.lambda.MaintainerInactivityLambda"
    });
    template.resourceCountIs('AWS::StepFunctions::StateMachine', 1);
    template.hasResourceProperties('AWS::StepFunctions::StateMachine', {
        "DefinitionString": {
            "Fn::Join": [
                "",
                [
                    "{\"StartAt\":\"Maintainer Inactivity Lambda\",\"States\":{\"Maintainer Inactivity Lambda\":{\"End\":true,\"Retry\":[{\"ErrorEquals\":[\"Lambda.ClientExecutionTimeoutException\",\"Lambda.ServiceException\",\"Lambda.AWSLambdaException\",\"Lambda.SdkClientException\"],\"IntervalSeconds\":2,\"MaxAttempts\":6,\"BackoffRate\":2},{\"ErrorEquals\":[\"States.ALL\"]}],\"Type\":\"Task\",\"TimeoutSeconds\":900,\"ResultPath\":null,\"Resource\":\"arn:",
                    {
                        "Ref": "AWS::Partition"
                    },
                    ":states:::lambda:invoke\",\"Parameters\":{\"FunctionName\":\"",
                    {
                        "Fn::GetAtt": [
                            "OpenSearchMetricsMaintainerInactivityLambdaCB6D4475",
                            "Arn"
                        ]
                    },
                    "\",\"Payload.$\":\"$\"}}},\"TimeoutSeconds\":900}"
                ]
            ]
        },
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchMaintainerInactivityWorkflowRoleF9A5E625",
                "Arn"
            ]
        },
        "StateMachineName": "OpenSearchMaintainerInactivityWorkflow"
    });
    template.hasResourceProperties('AWS::Events::Rule', {
        "ScheduleExpression": "cron(0 0 * * ? *)",
        "State": "ENABLED",
        "Targets": [
            {
                "Arn": {
                    "Ref": "OpenSearchMaintainerInactivityWorkflowE07E380B"
                },
                "Id": "Target0",
                "RoleArn": {
                    "Fn::GetAtt": [
                        "OpenSearchMaintainerInactivityWorkflowEventsRole0FDEAE61",
                        "Arn"
                    ]
                }
            }
        ]
    });
});
