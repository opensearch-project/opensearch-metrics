import {Duration, Stack, StackProps} from "aws-cdk-lib";
import { Construct } from 'constructs';
import { WorkflowComponent } from "./metricsWorkflow";
import { SnsMonitors } from "../constructs/snsMonitor";
import {OpenSearchLambda} from "../constructs/lambda";
import * as synthetics from "aws-cdk-lib/aws-synthetics";
import * as path from "path";
import * as secretsmanager from 'aws-cdk-lib/aws-secretsmanager';
import Project from "../enums/project";

interface OpenSearchMetricsMonitoringStackProps extends StackProps {
    readonly region: string;
    readonly account: string;
    readonly workflowComponent: WorkflowComponent;
    readonly lambdaPackage: string;
}

export class OpenSearchMetricsMonitoringStack extends Stack {

    private readonly slackLambda: OpenSearchLambda;

    constructor(scope: Construct, id: string, readonly props: OpenSearchMetricsMonitoringStackProps) {
        super(scope, id, props);

        const secretsName = 'slack-creds';
        const slackCredsSecrets = new secretsmanager.Secret(this, 'SlackApiCreds', {
            secretName: secretsName,
        });

        this.slackLambda = new OpenSearchLambda(this, "OpenSearchMetricsSlackLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsDashboardsSlackLambda",
            handler: "org.opensearchmetrics.lambda.SlackLambda",
            lambdaZipPath: `../../../build/distributions/${props.lambdaPackage}`,
            environment: {
                SLACK_CREDENTIALS_SECRETS: secretsName,
                SECRETS_MANAGER_REGION: slackCredsSecrets.env.region
            }
        });
        this.snsMonitorStepFunctionExecutionsFailed();
        this.snsMonitorCanaryFailed('metrics_heartbeat', `https://${Project.METRICS_HOSTED_ZONE}`);
    }

    /**
     * Create SNS alarms for failure StepFunction jobs.
     */
    private snsMonitorStepFunctionExecutionsFailed(): void {
        const stepFunctionSnsAlarms = [
            { alertName: 'StepFunction_execution_errors_MetricsWorkflow', stateMachineName: this.props.workflowComponent.opensearchMetricsWorkflowStateMachineName },
        ];

        new SnsMonitors(this, "SnsMonitors-StepFunctionExecutionsFailed", {
            region: this.props.region,
            accountId: this.props.account,
            stepFunctionSnsAlarms: stepFunctionSnsAlarms,
            alarmNameSpace: "AWS/States",
            snsTopic: "StepFunctionExecutionsFailed",
            slackLambda: this.slackLambda
        });
    }

    /**
     * Create SNS alarms for failure Canaries.
     */
    private snsMonitorCanaryFailed(canaryName: string, canaryUrl: string): void {
        const canary = new synthetics.Canary(this, 'CanaryHeartbeatMonitor', {
            canaryName: canaryName,
            schedule: synthetics.Schedule.rate(Duration.minutes(1)),
            test: synthetics.Test.custom({
                code: synthetics.Code.fromAsset(path.join(__dirname, '../../canary')),
                handler: 'index.handler',
            }),
            runtime: synthetics.Runtime.SYNTHETICS_NODEJS_PUPPETEER_6_2,
            environmentVariables: {
                SITE_URL: canaryUrl
            }
        });

        const canaryAlarms = [
            { alertName: 'Canary_failed_MetricsWorkflow', canary: canary },
        ];

        new SnsMonitors(this, "SnsMonitors-CanaryFailed", {
            region: this.props.region,
            accountId: this.props.account,
            canaryAlarms: canaryAlarms,
            alarmNameSpace: "CloudWatchSynthetics",
            snsTopic: "CanaryFailed",
            slackLambda: this.slackLambda
        });
    }
}