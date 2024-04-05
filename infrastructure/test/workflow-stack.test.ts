import {App} from "aws-cdk-lib";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchMetricsWorkflowStack} from "../lib/stacks/metricsWorkflow";
import Project from "../lib/enums/project";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {VpcStack} from "../lib/stacks/vpc";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";

test('Workflow Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'Test-OpenSearchHealth-VPC', {})
    const OpenSearchMetricsWorkflow = new OpenSearchMetricsWorkflowStack(app, 'Test-OpenSearchMetrics-Workflow', {
        opensearchDomainStack: new OpenSearchDomainStack(app, 'Test-OpenSearchHealth-OpenSearch', {
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
        }),
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE
    });
    const template = Template.fromStack(OpenSearchMetricsWorkflow);
    template.resourceCountIs('AWS::Lambda::Function', 1);
    template.resourceCountIs('AWS::IAM::Role', 2);
});