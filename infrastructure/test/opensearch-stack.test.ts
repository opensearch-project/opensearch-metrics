import {App} from "aws-cdk-lib";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";
import Project from "../lib/enums/project";
import {VpcStack} from "../lib/stacks/vpc";

test('OpenSearchDomain Stack Test', () => {
    const app = new App();
    const openSearchDomainStack = new OpenSearchDomainStack(app, 'Test-OpenSearchHealth-OpenSearch', {
        region: "us-east-1",
        account: "test-account",
        vpcStack: new VpcStack(app, 'Test-OpenSearchHealth-VPC', {}),
        enableNginxCognito: true,
        jenkinsAccess: {
            jenkinsAccountRoles:  [
                new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
            ]
        }
    });
    const openSearchDomainStackTemplate = Template.fromStack(openSearchDomainStack);
    openSearchDomainStackTemplate.resourceCountIs('AWS::IAM::Role', 8);
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPool', 1);
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPoolGroup', 1);
    openSearchDomainStackTemplate.resourceCountIs('AWS::IAM::Policy', 4);
});
