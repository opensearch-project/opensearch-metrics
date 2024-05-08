import {App} from "aws-cdk-lib";
import {VpcStack} from "../lib/stacks/vpc";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchHealthRoute53} from "../lib/stacks/route53";
import Project from "../lib/enums/project";

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
