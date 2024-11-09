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

export interface OpenSearchMaintainerInactivityWorkflowStackProps extends StackProps {
    readonly opensearchDomainStack: OpenSearchDomainStack;
    readonly vpcStack: VpcStack;
    readonly lambdaPackage: string
}

export interface WorkflowComponent {
    opensearchMaintainerInactivityWorkflowStateMachineName: string
}

export class OpenSearchMaintainerInactivityWorkflowStack extends Stack {
    public readonly workflowComponent: WorkflowComponent;
    constructor(scope: Construct, id: string, props: OpenSearchMaintainerInactivityWorkflowStackProps) {
        super(scope, id, props);

        const maintainerInactivityTask = this.createMaintainerInactivityTask(
            this,
            props.opensearchDomainStack,
            props.vpcStack,
            props.lambdaPackage
        );
        const opensearchMaintainerInactivityWorkflow = new StateMachine(this, 'OpenSearchMaintainerInactivityWorkflow', {
            definition: maintainerInactivityTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchMaintainerInactivityWorkflow'
        })

        new Rule(this, 'MaintainerInactivityWorkflow-Every-Day', {
            schedule: Schedule.expression('cron(0 0 * * ? *)'),
            targets: [new SfnStateMachine(opensearchMaintainerInactivityWorkflow)],
        });

        this.workflowComponent = {
            opensearchMaintainerInactivityWorkflowStateMachineName: opensearchMaintainerInactivityWorkflow.stateMachineName
        }
    }

    private createMaintainerInactivityTask(scope: Construct, opensearchDomainStack: OpenSearchDomainStack,
                              vpcStack: VpcStack, lambdaPackage: string) {
        const openSearchDomain = opensearchDomainStack.domain;
        const maintainerInactivityLambda = new OpenSearchLambda(scope, "OpenSearchMetricsMaintainerInactivityLambdaFunction", {
            lambdaNameBase: "OpenSearchMetricsMaintainerInactivity",
            handler: "org.opensearchmetrics.lambda.MaintainerInactivityLambda",
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
        return new LambdaInvoke(scope, 'Maintainer Inactivity Lambda', {
            lambdaFunction: maintainerInactivityLambda,
            resultPath: JsonPath.DISCARD,
            timeout: Duration.minutes(15)
        }).addRetry();
    }
}
