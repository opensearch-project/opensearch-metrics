import {App} from "aws-cdk-lib";
import {Template} from "aws-cdk-lib/assertions";
import Project from "../lib/enums/project";
import {OpenSearchMetricsNginxReadonly} from "../lib/stacks/opensearchNginxProxyReadonly";
import {VpcStack} from "../lib/stacks/vpc";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchHealthRoute53} from "../lib/stacks/route53";

test('NginxProxyReadonly Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'Test-OpenSearchHealth-VPC', {});
    const openSearchDomainStack = new OpenSearchDomainStack(app, 'Test-OpenSearchHealth-OpenSearch', {
        region: "us-east-1",
        account: "test-account",
        vpcStack: vpcStack,
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles:  [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const metricsHostedZone = new OpenSearchHealthRoute53(app, 'Test-OpenSearchMetrics-HostedZone', {
        hostedZone: Project.METRICS_HOSTED_ZONE,
        appName: "OpenSearchMetrics"
    });
    const openSearchMetricsNginxReadonlyStack = new OpenSearchMetricsNginxReadonly(app, 'Test-OpenSearchMetricsNginxReadonly', {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        vpc: vpcStack.vpc,
        securityGroup: vpcStack.securityGroup,
        opensearchDashboardUrlProps: {
            opensearchDashboardVpcUrl: openSearchDomainStack.domain.domainEndpoint,
            openSearchDomainName: openSearchDomainStack.domain.domainName
        },
        albProps: {
            hostedZone: metricsHostedZone,
            certificateArn: metricsHostedZone.certificateArn,
        },
    });
    const openSearchMetricsNginxReadonlyTemplate = Template.fromStack(openSearchMetricsNginxReadonlyStack);
    openSearchMetricsNginxReadonlyTemplate.resourceCountIs('AWS::Route53::RecordSet', 1);
    openSearchMetricsNginxReadonlyTemplate.hasResourceProperties('AWS::Route53::RecordSet', {
        "Type": "A"
    });
});