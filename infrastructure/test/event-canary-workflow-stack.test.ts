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
import Project from "../lib/enums/project";
import { VpcStack } from "../lib/stacks/vpc";
import {OpenSearchEventCanaryWorkflowStack} from "../lib/stacks/eventCanaryWorkflow";
import {OpenSearchMetricsSecretsStack} from "../lib/stacks/secrets";

test('Event Canary Workflow Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'Test-OpenSearchHealth-VPC', {});

    // Create Secret Manager for the metrics project
    const openSearchMetricsSecretsStack = new OpenSearchMetricsSecretsStack(app, "OpenSearchMetrics-Secrets", {
        secretName: 'metrics-creds'
    });

    const openSearchEventCanaryWorkflowStack = new OpenSearchEventCanaryWorkflowStack(app, 'OpenSearchEventCanary-Workflow', {
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE,
        gitHubRepoTarget: Project.EVENT_CANARY_REPO_TARGET,
        gitHubAppSecret: openSearchMetricsSecretsStack.secret,
    })

    openSearchEventCanaryWorkflowStack.node.addDependency(vpcStack);
    const template = Template.fromStack(openSearchEventCanaryWorkflowStack);
    template.resourceCountIs('AWS::IAM::Role', 3);
    template.resourceCountIs('AWS::Lambda::Function', 1);
    template.hasResourceProperties('AWS::Lambda::Function', {
        "FunctionName": "OpenSearchMetricsEventCanaryLambda",
        "Handler": "org.opensearchmetrics.lambda.EventCanaryLambda"
    });
    template.resourceCountIs('AWS::StepFunctions::StateMachine', 1);
    template.hasResourceProperties('AWS::StepFunctions::StateMachine', {
        "DefinitionString": {
            "Fn::Join": [
                "",
                [
                    "{\"StartAt\":\"Event Canary Lambda\",\"States\":{\"Event Canary Lambda\":{\"End\":true,\"Retry\":[{\"ErrorEquals\":[\"Lambda.ClientExecutionTimeoutException\",\"Lambda.ServiceException\",\"Lambda.AWSLambdaException\",\"Lambda.SdkClientException\"],\"IntervalSeconds\":2,\"MaxAttempts\":6,\"BackoffRate\":2},{\"ErrorEquals\":[\"States.ALL\"]}],\"Type\":\"Task\",\"TimeoutSeconds\":900,\"ResultPath\":null,\"Resource\":\"arn:",
                    {
                        "Ref": "AWS::Partition"
                    },
                    ":states:::lambda:invoke\",\"Parameters\":{\"FunctionName\":\"",
                    {
                        "Fn::GetAtt": [
                            "OpenSearchMetricsEventCanaryLambda358BAA07",
                            "Arn"
                        ]
                    },
                    "\",\"Payload.$\":\"$\"}}},\"TimeoutSeconds\":900}"
                ]
            ]
        },
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchEventCanaryWorkflowRoleDC920D0E",
                "Arn"
            ]
        },
        "StateMachineName": "OpenSearchEventCanaryWorkflow"
    });
    template.resourceCountIs('AWS::Events::Rule', 1);
    template.hasResourceProperties('AWS::Events::Rule', {
        "ScheduleExpression": "cron(0/10 * * * ? *)",
        "State": "ENABLED",
        "Targets": [
            {
                "Arn": {
                    "Ref": "OpenSearchEventCanaryWorkflowEB1017B7"
                },
                "Id": "Target0",
                "RoleArn": {
                    "Fn::GetAtt": [
                        "OpenSearchEventCanaryWorkflowEventsRoleA5644829",
                        "Arn"
                    ]
                }
            }
        ]
    });
});
