/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import Project from "../lib/enums/project";
import { OpenSearchDomainStack } from "../lib/stacks/opensearch";
import { VpcStack } from "../lib/stacks/vpc";
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import {OpenSearchS3} from "../lib/stacks/s3";
import {OpenSearchS3EventIndexWorkflowStack} from "../lib/stacks/s3EventIndexWorkflow";

test('S3 Event Index Workflow Stack Test', () => {
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
    const OpenSearchS3EventIndexWorkflow = new OpenSearchS3EventIndexWorkflowStack(app, 'Test-OpenSearchS3EventIndex-Workflow', {
        region: Project.REGION,
        opensearchDomainStack: openSearchDomainStack,
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE,
        githubEventsBucket: s3Stack.bucket
    });
    const template = Template.fromStack(OpenSearchS3EventIndexWorkflow);
    template.resourceCountIs('AWS::IAM::Role', 2);
    template.resourceCountIs('AWS::Lambda::Function', 1);
    template.hasResourceProperties('AWS::Lambda::Function', {
        "FunctionName": "OpenSearchMetricsS3EventIndexLambda",
        "Handler": "org.opensearchmetrics.lambda.GithubEventsLambda"
    });
    template.resourceCountIs('AWS::StepFunctions::StateMachine', 1);
    template.hasResourceProperties('AWS::StepFunctions::StateMachine', {
        "DefinitionString": {
            "Fn::Join": [
                "",
                [
                    "{\"StartAt\":\"S3 Event Index Lambda\",\"States\":{\"S3 Event Index Lambda\":{\"End\":true,\"Retry\":[{\"ErrorEquals\":[\"Lambda.ClientExecutionTimeoutException\",\"Lambda.ServiceException\",\"Lambda.AWSLambdaException\",\"Lambda.SdkClientException\"],\"IntervalSeconds\":2,\"MaxAttempts\":6,\"BackoffRate\":2},{\"ErrorEquals\":[\"States.ALL\"]}],\"Type\":\"Task\",\"TimeoutSeconds\":900,\"ResultPath\":null,\"Resource\":\"arn:",
                    {
                        "Ref": "AWS::Partition"
                    },
                    ":states:::lambda:invoke\",\"Parameters\":{\"FunctionName\":\"",
                    {
                        "Fn::GetAtt": [
                            "OpenSearchMetricsS3EventIndexLambda6B9F9C68",
                            "Arn"
                        ]
                    },
                    "\",\"Payload.$\":\"$\"}}},\"TimeoutSeconds\":900}"
                ]
            ]
        },
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchS3EventIndexWorkflowRoleE2712A63",
                "Arn"
            ]
        },
        "StateMachineName": "OpenSearchS3EventIndexWorkflow"
    });
});
