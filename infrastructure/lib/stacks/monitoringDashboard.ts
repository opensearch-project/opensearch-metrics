/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Duration, Stack, StackProps } from "aws-cdk-lib";
import { Effect, PolicyStatement, Role, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Canary, Code, Runtime, Schedule, Test } from "aws-cdk-lib/aws-synthetics";
import { Construct } from 'constructs';
import * as path from "path";
import { canarySns } from "../constructs/canarySns";
import { OpenSearchLambda } from "../constructs/lambda";
import { StepFunctionSns } from "../constructs/stepFunctionSns";
import Project from "../enums/project";
import { WorkflowComponent } from "./metricsWorkflow";
import { VpcStack } from "./vpc";


interface OpenSearchMetricsMonitoringStackProps extends StackProps {
    readonly region: string;
    readonly account: string;
    readonly workflowComponent: WorkflowComponent;
    readonly lambdaPackage: string;
    readonly secrets: Secret;
    readonly vpcStack: VpcStack;
}

export class OpenSearchMetricsMonitoringStack extends Stack {

    private readonly slackLambda: OpenSearchLambda;

    constructor(scope: Construct, id: string, readonly props: OpenSearchMetricsMonitoringStackProps) {
        super(scope, id, props);

        const slackLambdaRole = new Role(this, 'OpenSearchSlackLambdaRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: "OpenSearch Metrics Slack Lambda Execution Role",
            roleName: "OpenSearchSlackLambdaRole"
        });

        slackLambdaRole.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["secretsmanager:GetSecretValue"],
                resources: [`${props.secrets.secretFullArn}`],
            }),
        );

        this.slackLambda = new OpenSearchLambda(this, "OpenSearchMetricsSlackLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsDashboardsSlackLambda",
            handler: "org.opensearchmetrics.lambda.SlackLambda",
            lambdaZipPath: `../../../build/distributions/${props.lambdaPackage}`,
            role: slackLambdaRole,
            environment: {
                SLACK_CREDENTIALS_SECRETS: props.secrets.secretName,
                SECRETS_MANAGER_REGION: props.secrets.env.region
            }
        });
        this.snsMonitorStepFunctionExecutionsFailed();
        this.snsMonitorCanaryFailed('metrics_heartbeat', `https://${Project.METRICS_HOSTED_ZONE}`, props.vpcStack);
    }

    /**
     * Create SNS alarms for failure StepFunction jobs.
     */
    private snsMonitorStepFunctionExecutionsFailed(): void {
        const stepFunctionSnsAlarms = [
            { alertName: 'StepFunction_execution_errors_MetricsWorkflow', stateMachineName: this.props.workflowComponent.opensearchMetricsWorkflowStateMachineName },
        ];

        new StepFunctionSns(this, "SnsMonitors-StepFunctionExecutionsFailed", {
            region: this.props.region,
            accountId: this.props.account,
            stepFunctionSnsAlarms: stepFunctionSnsAlarms,
            alarmNameSpace: "AWS/States",
            snsTopicName: "StepFunctionExecutionsFailed",
            slackLambda: this.slackLambda
        });
    }

    /**
     * Create SNS alarms for failure Canaries.
     */
    private snsMonitorCanaryFailed(canaryName: string, canaryUrl: string, vpcStack: VpcStack): void {
        const canary = new Canary(this, 'CanaryHeartbeatMonitor', {
            canaryName: canaryName,
            schedule: Schedule.rate(Duration.minutes(5)),
            test: Test.custom({
                code: Code.fromAsset(path.join(__dirname, '../../canary')),
                handler: 'urlMonitor.handler',
            }),
            runtime: Runtime.SYNTHETICS_NODEJS_PUPPETEER_7_0,
            environmentVariables: {
                SITE_URL: canaryUrl
            },
            vpc: vpcStack.vpc,
            vpcSubnets: vpcStack.subnets,
            securityGroups: [vpcStack.securityGroup],
        });

        const canaryAlarms = [
            { alertName: 'Canary_failed_MetricsWorkflow', canary: canary },
        ];

        new canarySns(this, "SnsMonitors-CanaryFailed", {
            region: this.props.region,
            accountId: this.props.account,
            canaryAlarms: canaryAlarms,
            alarmNameSpace: "CloudWatchSynthetics",
            snsTopicName: "CanaryFailed",
            slackLambda: this.slackLambda
        });
    }
}

