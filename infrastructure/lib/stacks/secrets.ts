/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Stack } from "aws-cdk-lib";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Construct } from 'constructs';

export interface SecretProps {
    readonly secretName: string
}

export class OpenSearchMetricsSecretsStack extends Stack {
    readonly secret: Secret;

    constructor(scope: Construct, id: string, props: SecretProps) {
        super(scope, id);
        this.secret = new Secret(this, `MetricsCreds-${props.secretName}`, {
            secretName: props.secretName,
        });
    }
}
