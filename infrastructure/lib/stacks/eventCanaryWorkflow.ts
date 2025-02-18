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
import {Effect, ManagedPolicy, PolicyDocument, PolicyStatement, Role, ServicePrincipal} from "aws-cdk-lib/aws-iam";
import {Secret} from "aws-cdk-lib/aws-secretsmanager";

export interface OpenSearchEventCanaryWorkflowStackProps extends StackProps {
    readonly vpcStack: VpcStack;
    readonly lambdaPackage: string;
    readonly gitHubRepoTarget: string;
    readonly gitHubAppSecret: Secret;
}

export interface WorkflowComponent {
    opensearchEventCanaryWorkflowStateMachineName: string
}

export class OpenSearchEventCanaryWorkflowStack extends Stack {
    public readonly workflowComponent: WorkflowComponent;
    constructor(scope: Construct, id: string, props: OpenSearchEventCanaryWorkflowStackProps) {
        super(scope, id, props);

        const eventCanaryTask = this.createEventCanaryTask(this,
            props.vpcStack,
            props.lambdaPackage,
            props.gitHubRepoTarget,
            props.gitHubAppSecret,
        );

        const opensearchEventCanaryWorkflow = new StateMachine(this, 'OpenSearchEventCanaryWorkflow', {
            definition: eventCanaryTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchEventCanaryWorkflow'
        })

        new Rule(this, 'OpenSearchEventCanaryWorkflow-Every-10mins', {
            schedule: Schedule.expression('cron(0/10 * * * ? *)'),
            targets: [new SfnStateMachine(opensearchEventCanaryWorkflow)],
        });

        this.workflowComponent = {
            opensearchEventCanaryWorkflowStateMachineName: opensearchEventCanaryWorkflow.stateMachineName
        }
    }

    private createEventCanaryTask(scope: Construct, vpcStack: VpcStack, lambdaPackage: string, gitHubRepoTarget: string, gitHubAppSecret: Secret) {
        const eventCanaryLambdaRole = new Role(this, 'OpenSearchEventCanaryLambdaRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: "OpenSearch Metrics Event Canary Lambda Execution Role",
            roleName: "OpenSearchEventCanaryLambdaRole",
            managedPolicies: [
                ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'),
                ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaVPCAccessExecutionRole'),
            ]
        });

        eventCanaryLambdaRole.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["secretsmanager:GetSecretValue"],
                resources: [`${gitHubAppSecret.secretFullArn}`],
            }),
        );

        const eventCanaryLambda = new OpenSearchLambda(this, "OpenSearchMetricsEventCanaryLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsEventCanary",
            handler: "org.opensearchmetrics.lambda.EventCanaryLambda",
            lambdaZipPath: `../../../build/distributions/${lambdaPackage}`,
            vpc: vpcStack.vpc,
            securityGroup: vpcStack.securityGroup,
            role: eventCanaryLambdaRole,
            environment: {
                GITHUB_REPO_TARGET: gitHubRepoTarget,
                API_CREDENTIALS_SECRETS: gitHubAppSecret.secretName,
                SECRETS_MANAGER_REGION: gitHubAppSecret.env.region,
            }
        }).lambda;
        return new LambdaInvoke(scope, 'Event Canary Lambda', {
            lambdaFunction: eventCanaryLambda,
            resultPath: JsonPath.DISCARD,
            timeout: Duration.minutes(15)
        }).addRetry();
    }
}
