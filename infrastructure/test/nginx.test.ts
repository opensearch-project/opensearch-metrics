import {App} from "aws-cdk-lib";
import {VpcStack} from "../lib/stacks/vpc";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchMetricsNginxReadonly} from "../lib/stacks/opensearchNginxProxyReadonly";
import Project from "../lib/enums/project";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchHealthRoute53} from "../lib/stacks/route53";

test('Nginx Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        vpcStack: vpcStack,
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles:  [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const metricsHostedZone = new OpenSearchHealthRoute53(app, "OpenSearchMetrics-HostedZone", {
        hostedZone: Project.METRICS_HOSTED_ZONE,
        appName: "OpenSearchMetrics"
    });
    const stack = new OpenSearchMetricsNginxReadonly(app, 'Test-OpenSearchMetricsNginxReadonly', {
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
    const template = Template.fromStack(stack);
    template.resourceCountIs('AWS::EC2::SecurityGroup', 2);
    template.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupEgress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow all outbound traffic by default",
                "IpProtocol": "-1"
            }
        ]
    });
});