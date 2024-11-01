/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Stack } from "aws-cdk-lib";
import { Certificate, CertificateValidation } from "aws-cdk-lib/aws-certificatemanager";
import { HostedZone } from "aws-cdk-lib/aws-route53";
import { Construct } from "constructs";

export interface Route53Props {
    readonly hostedZone: string,
    readonly appName: string,
}

export class OpenSearchHealthRoute53 extends Stack {
    readonly zone: HostedZone;
    readonly certificateArn: string
    constructor(scope: Construct, id: string, props: Route53Props) {

        super(scope, id);

        this.zone = new HostedZone(this, `${props.appName}.-HostedZone`, {
            zoneName: props.hostedZone,
        });

        const certificate = new Certificate(this, `${props.appName}-Certificate`, {
            domainName: props.hostedZone,
            certificateName: props.appName,
            validation: CertificateValidation.fromDns(this.zone),
        });
        this.certificateArn = certificate.certificateArn;
    }
}