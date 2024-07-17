/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Duration } from 'aws-cdk-lib';
import { ISecurityGroup, IVpc, SubnetType } from "aws-cdk-lib/aws-ec2";
import { IRole } from "aws-cdk-lib/aws-iam";
import { Code, Function, Runtime, Tracing } from 'aws-cdk-lib/aws-lambda';
import { Construct } from 'constructs';
import * as path from 'path';

export class OpenSearchLambdaProps {
    readonly lambdaNameBase: string;
    readonly lambdaZipPath: string;
    readonly handler: string;
    readonly vpc?: IVpc;
    readonly securityGroup?: ISecurityGroup;
    readonly role?: IRole;
    readonly environment?: {
        [key: string]: string;
    };
}

export class OpenSearchLambda extends Construct {
    readonly lambda: Function;

    constructor(scope: Construct, id: string, props: OpenSearchLambdaProps) {
        super(scope, id);

        const resourceGenerationTime = new Date().toISOString();
        this.lambda = new Function(scope, `${props.lambdaNameBase}Lambda`, {
            vpc: props.vpc,
            vpcSubnets: props.vpc?.selectSubnets({
                subnetType: SubnetType.PRIVATE_WITH_EGRESS
            }),
            securityGroups: props.securityGroup ? [props.securityGroup] : undefined,
            role: props.role ? props.role : undefined,
            code: Code.fromAsset(path.join(__dirname, props.lambdaZipPath)),
            handler: props.handler,
            timeout: Duration.minutes(15),
            runtime: Runtime.JAVA_17,
            tracing: Tracing.ACTIVE,
            memorySize: 1024,
            description: `Generated on: ${resourceGenerationTime}`,
            functionName: `${props.lambdaNameBase}Lambda`,
            environment: this.getEnvVariables(props.environment)
        });
    }

    private getEnvVariables(environment: { [p: string]: string } | undefined) {
        const defaults = {};
        if (environment) {
            return { ...environment, ...defaults }
        }
        return defaults;
    }
}