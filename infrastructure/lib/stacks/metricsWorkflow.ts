import { OpenSearchDomainStack } from "./opensearch";
import {OpenSearchLambda} from "../constructs/lambda";
import {Duration, Stack, StackProps} from "aws-cdk-lib";
import { Construct } from 'constructs';
import { VpcStack } from "./vpc";
import {LambdaInvoke} from "aws-cdk-lib/aws-stepfunctions-tasks";
import {JsonPath, StateMachine} from "aws-cdk-lib/aws-stepfunctions";
import {Rule, Schedule} from "aws-cdk-lib/aws-events";
import {SfnStateMachine} from "aws-cdk-lib/aws-events-targets";

export interface OpenSearchMetricsStackProps extends StackProps {
    readonly opensearchDomainStack: OpenSearchDomainStack;
    readonly vpcStack: VpcStack;
}
export class OpenSearchMetricsWorkflowStack extends Stack {
    constructor(scope: Construct, id: string, props: OpenSearchMetricsStackProps) {
        super(scope, id, props);

        const metricsTask = this.createMetricsTask(
            this,
            props.opensearchDomainStack,
            props.vpcStack,
        );
        const opensearchMetricsWorkflow = new StateMachine(this, 'OpenSearchMetricsWorkflow', {
            definition: metricsTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchMetricsWorkflow'
        })

        new Rule(this, 'MetricsWorkflow', {
            schedule: Schedule.expression('cron(0 18 * * ? *)'),
            targets: [new SfnStateMachine(opensearchMetricsWorkflow)],
        });
    }

    private createMetricsTask(scope: Construct, opensearchDomainStack: OpenSearchDomainStack,
                             vpcStack: VpcStack) {
        const openSearchDomain = opensearchDomainStack.domain;
        const metricsLambda = new OpenSearchLambda(scope, "OpenSearchMetricsLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsDashboards",
            handler: "org.opensearchmetrics.lambda.MetricsLambda",
            lambdaZipPath: "../../../build/distributions/opensearch-insights-1.0-SNAPSHOT.zip",
            vpc: vpcStack.vpc,
            securityGroup: vpcStack.securityGroup,
            role: opensearchDomainStack.openSearchLambdaRole,
            environment: {
                OPENSEARCH_DOMAIN_ENDPOINT: openSearchDomain.domainEndpoint,
                OPENSEARCH_DOMAIN_REGION: openSearchDomain.env.region,
                OPENSEARCH_DOMAIN_ROLE: opensearchDomainStack.fullAccessRole.roleArn,
            },
        }).lambda;
        return new LambdaInvoke(scope, 'Metrics Lambda', {
            lambdaFunction: metricsLambda,
            resultPath: JsonPath.DISCARD,
            timeout: Duration.minutes(15)
        }).addRetry();
    }
}
