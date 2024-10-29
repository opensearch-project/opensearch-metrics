/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Duration, Stack, StackProps } from "aws-cdk-lib";
import { Rule, RuleTargetInput, Schedule } from "aws-cdk-lib/aws-events";
import { SfnStateMachine } from "aws-cdk-lib/aws-events-targets";
import { Bucket } from "aws-cdk-lib/aws-s3";
import { JsonPath, StateMachine, TaskInput } from "aws-cdk-lib/aws-stepfunctions";
import { LambdaInvoke } from "aws-cdk-lib/aws-stepfunctions-tasks";
import { Construct } from 'constructs';
import { OpenSearchLambda } from "../constructs/lambda";
import { OpenSearchDomainStack } from "./opensearch";
import { VpcStack } from "./vpc";

export interface OpenSearchS3EventIndexWorkflowStackProps extends StackProps {
    readonly region: string;
    readonly opensearchDomainStack: OpenSearchDomainStack;
    readonly vpcStack: VpcStack;
    readonly lambdaPackage: string;
    readonly githubEventsBucket: Bucket;
}

export interface WorkflowComponent {
    opensearchS3EventIndexWorkflowStateMachineName: string
}
export class OpenSearchS3EventIndexWorkflowStack extends Stack {
    public readonly workflowComponent: WorkflowComponent;
    constructor(scope: Construct, id: string, props: OpenSearchS3EventIndexWorkflowStackProps) {
        super(scope, id, props);

        const s3EventIndexTask = this.createS3EventIndexTask(this,
            props.region,
            props.opensearchDomainStack,
            props.vpcStack,
            props.lambdaPackage,
            props.githubEventsBucket);

        const opensearchS3EventIndexWorkflow = new StateMachine(this, 'OpenSearchS3EventIndexWorkflow', {
            definition: s3EventIndexTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchS3EventIndexWorkflow'
        })

        const rule = new Rule(this, 'OpenSearchS3EventIndexWorkflow-Every-Day', {
            schedule: Schedule.expression('cron(0 0 * * ? *)'),
        });

        rule.addTarget(new SfnStateMachine(opensearchS3EventIndexWorkflow, {
            input: RuleTargetInput.fromObject({}),
        }));

        this.workflowComponent = {
            opensearchS3EventIndexWorkflowStateMachineName: opensearchS3EventIndexWorkflow.stateMachineName
        }
    }

    private createS3EventIndexTask(scope: Construct, region: string, opensearchDomainStack: OpenSearchDomainStack, vpcStack: VpcStack, lambdaPackage: string, githubEventsBucket: Bucket) {
        const openSearchDomain = opensearchDomainStack.domain;
        const s3EventIndexLambda = new OpenSearchLambda(this, "OpenSearchMetricsS3EventIndexLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsS3EventIndex",
            handler: "org.opensearchmetrics.lambda.GithubEventsLambda",
            lambdaZipPath: `../../../build/distributions/${lambdaPackage}`,
            vpc: vpcStack.vpc,
            securityGroup: vpcStack.securityGroup,
            role: opensearchDomainStack.openSearchS3EventsIndexLambdaRole,
            environment: {
                S3_BUCKET_REGION: region,
                EVENT_BUCKET_NAME: githubEventsBucket.bucketName,
                OPENSEARCH_DOMAIN_ENDPOINT: openSearchDomain.domainEndpoint,
                OPENSEARCH_DOMAIN_REGION: openSearchDomain.env.region,
                OPENSEARCH_DOMAIN_ROLE: opensearchDomainStack.fullAccessRole.roleArn,
            }
        }).lambda;
        return new LambdaInvoke(scope, 'S3 Event Index Lambda', {
            lambdaFunction: s3EventIndexLambda,
            resultPath: JsonPath.DISCARD,
            payload: TaskInput.fromJsonPathAt("$"),
            timeout: Duration.minutes(15)
        }).addRetry();
    }
}
