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
import { OpenSearchMetricsWorkflowStack } from "../lib/stacks/metricsWorkflow";
import { OpenSearchDomainStack } from "../lib/stacks/opensearch";
import { OpenSearchS3 } from "../lib/stacks/s3";
import { VpcStack } from "../lib/stacks/vpc";

test('Metrics Workflow Stack Test', () => {
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
    const OpenSearchMetricsWorkflow = new OpenSearchMetricsWorkflowStack(app, 'Test-OpenSearchMetrics-Workflow', {
        opensearchDomainStack: openSearchDomainStack,
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE,
    });
    const template = Template.fromStack(OpenSearchMetricsWorkflow);
    template.resourceCountIs('AWS::IAM::Role', 2);
    template.resourceCountIs('AWS::Lambda::Function', 1);
    template.hasResourceProperties('AWS::Lambda::Function', {
        "FunctionName": "OpenSearchMetricsDashboardsLambda",
        "Handler": "org.opensearchmetrics.lambda.MetricsLambda"
    });
    template.resourceCountIs('AWS::StepFunctions::StateMachine', 1);
    template.hasResourceProperties('AWS::StepFunctions::StateMachine', {
        "DefinitionString": {
            "Fn::Join": [
                "",
                [
                    "{\"StartAt\":\"Metrics Lambda\",\"States\":{\"Metrics Lambda\":{\"End\":true,\"Retry\":[{\"ErrorEquals\":[\"Lambda.ClientExecutionTimeoutException\",\"Lambda.ServiceException\",\"Lambda.AWSLambdaException\",\"Lambda.SdkClientException\"],\"IntervalSeconds\":2,\"MaxAttempts\":6,\"BackoffRate\":2},{\"ErrorEquals\":[\"States.ALL\"]}],\"Type\":\"Task\",\"TimeoutSeconds\":900,\"ResultPath\":null,\"Resource\":\"arn:",
                    {
                        "Ref": "AWS::Partition"
                    },
                    ":states:::lambda:invoke\",\"Parameters\":{\"FunctionName\":\"",
                    {
                        "Fn::GetAtt": [
                            "OpenSearchMetricsDashboardsLambda6F1E44E4",
                            "Arn"
                        ]
                    },
                    "\",\"Payload.$\":\"$\"}}},\"TimeoutSeconds\":900}"
                ]
            ]
        },
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchMetricsWorkflowRole3ECE841A",
                "Arn"
            ]
        },
        "StateMachineName": "OpenSearchMetricsWorkflow"
    });
});
