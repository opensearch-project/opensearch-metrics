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
    const hostedZoneCognitoStack = new OpenSearchHealthRoute53(app, 'Test-OpenSearchMetrics-HostedZoneCognito', {
        hostedZone: Project.METRICS_COGNITO_HOSTED_ZONE,
        appName: "OpenSearchMetricsCognito"
    });
    const hostedZoneStackTemplate = Template.fromStack(hostedZoneStack);

    hostedZoneStackTemplate.resourceCountIs('AWS::Route53::HostedZone', 1);
    hostedZoneStackTemplate.hasResourceProperties('AWS::Route53::HostedZone', {
        "Name": "metrics.opensearch.org."
    });
    hostedZoneStackTemplate.hasResourceProperties('AWS::CertificateManager::Certificate', {
        "DomainName": "metrics.opensearch.org"
    });

    const hostedZoneCognitoStackTemplate = Template.fromStack(hostedZoneCognitoStack);
    hostedZoneCognitoStackTemplate.resourceCountIs('AWS::Route53::HostedZone', 1);
    hostedZoneCognitoStackTemplate.hasResourceProperties('AWS::Route53::HostedZone', {
        "Name": "metrics.login.opensearch.org."
    });
    hostedZoneCognitoStackTemplate.hasResourceProperties('AWS::CertificateManager::Certificate', {
        "DomainName": "metrics.login.opensearch.org"
    });
});