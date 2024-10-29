/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Duration, Stack, StackProps } from "aws-cdk-lib";
import { Rule, Schedule } from "aws-cdk-lib/aws-events";
import { SfnStateMachine } from "aws-cdk-lib/aws-events-targets";
import { JsonPath, StateMachine } from "aws-cdk-lib/aws-stepfunctions";
import { LambdaInvoke } from "aws-cdk-lib/aws-stepfunctions-tasks";
import { Construct } from 'constructs';
import { OpenSearchLambda } from "../constructs/lambda";
import { OpenSearchDomainStack } from "./opensearch";
import { VpcStack } from "./vpc";

export interface OpenSearchMetricsStackProps extends StackProps {
    readonly opensearchDomainStack: OpenSearchDomainStack;
    readonly vpcStack: VpcStack;
    readonly lambdaPackage: string
}

export interface WorkflowComponent {
    opensearchMetricsWorkflowStateMachineName: string
}
export class OpenSearchMetricsWorkflowStack extends Stack {
    public readonly workflowComponent: WorkflowComponent;
    constructor(scope: Construct, id: string, props: OpenSearchMetricsStackProps) {
        super(scope, id, props);

        const metricsTask = this.createMetricsTask(
            this,
            props.opensearchDomainStack,
            props.vpcStack,
            props.lambdaPackage
        );
        const opensearchMetricsWorkflow = new StateMachine(this, 'OpenSearchMetricsWorkflow', {
            definition: metricsTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchMetricsWorkflow'
        })

        new Rule(this, 'MetricsWorkflow-Every-3hrs', {
            schedule: Schedule.rate(Duration.hours(3)),
            targets: [new SfnStateMachine(opensearchMetricsWorkflow)],
        });

        this.workflowComponent = {
            opensearchMetricsWorkflowStateMachineName: opensearchMetricsWorkflow.stateMachineName
        }
    }

    private createMetricsTask(scope: Construct, opensearchDomainStack: OpenSearchDomainStack,
        vpcStack: VpcStack, lambdaPackage: string) {
        const openSearchDomain = opensearchDomainStack.domain;
        const metricsLambda = new OpenSearchLambda(scope, "OpenSearchMetricsLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsDashboards",
            handler: "org.opensearchmetrics.lambda.MetricsLambda",
            lambdaZipPath: `../../../build/distributions/${lambdaPackage}`,
            vpc: vpcStack.vpc,
            securityGroup: vpcStack.securityGroup,
            role: opensearchDomainStack.openSearchMetricsLambdaRole,
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
