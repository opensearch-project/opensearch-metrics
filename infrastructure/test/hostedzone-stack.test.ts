/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import Project from "../lib/enums/project";
import { OpenSearchHealthRoute53 } from "../lib/stacks/route53";

test('HostedZone Stack Test', () => {
    const app = new App();
    const hostedZoneStack = new OpenSearchHealthRoute53(app, 'Test-OpenSearchMetrics-HostedZone', {
        hostedZone: Project.METRICS_HOSTED_ZONE,
        appName: "OpenSearchMetrics"
    });
    const hostedZoneStackTemplate = Template.fromStack(hostedZoneStack);
    hostedZoneStackTemplate.resourceCountIs('AWS::Route53::HostedZone', 1);
    hostedZoneStackTemplate.hasResourceProperties('AWS::Route53::HostedZone', {
        "Name": "metrics.opensearch.org."
    });
    hostedZoneStackTemplate.hasResourceProperties('AWS::CertificateManager::Certificate', {
        "DomainName": "metrics.opensearch.org"
    });
});

test('HostedZoneCognito Stack Test', () => {
    const app = new App();
    const hostedZoneCognitoStack = new OpenSearchHealthRoute53(app, 'Test-OpenSearchMetrics-HostedZoneCognito', {
        hostedZone: Project.METRICS_COGNITO_HOSTED_ZONE,
        appName: "OpenSearchMetricsCognito"
    });
    const hostedZoneCognitoStackTemplate = Template.fromStack(hostedZoneCognitoStack);
    hostedZoneCognitoStackTemplate.resourceCountIs('AWS::Route53::HostedZone', 1);
    hostedZoneCognitoStackTemplate.hasResourceProperties('AWS::Route53::HostedZone', {
        "Name": `${Project.METRICS_COGNITO_HOSTED_ZONE}.`
    });
    hostedZoneCognitoStackTemplate.hasResourceProperties('AWS::CertificateManager::Certificate', {
        "DomainName": `${Project.METRICS_COGNITO_HOSTED_ZONE}`
    });
});
