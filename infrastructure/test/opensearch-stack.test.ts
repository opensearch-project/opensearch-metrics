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
    openSearchDomainStackTemplate.hasResourceProperties('AWS::IAM::Role', {
        "AssumeRolePolicyDocument": {
            "Statement": [
                {
                    "Action": "sts:AssumeRoleWithWebIdentity",
                    "Condition": {
                        "StringEquals": {
                            "cognito-identity.amazonaws.com:aud": {
                                "Ref": "IdentityPool"
                            }
                        },
                        "ForAnyValue:StringLike": {
                            "cognito-identity.amazonaws.com:amr": "authenticated"
                        }
                    },
                    "Effect": "Allow",
                    "Principal": {
                        "Federated": "cognito-identity.amazonaws.com"
                    }
                }
            ],
            "Version": "2012-10-17"
        }
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::UserPoolGroup', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Cognito::UserPoolGroup', {
        "GroupName": "opensearch-admin-group",
        "RoleArn": {
            "Fn::GetAtt": [
                "OpenSearchHealthCognitoCognitoopensearchhealthidentitypoolAdminRole3AC37B11",
                "Arn"
            ]
        },
        "UserPoolId": {
            "Ref": "UserPool"
        }
    });
    openSearchDomainStackTemplate.resourceCountIs('AWS::Cognito::IdentityPoolRoleAttachment', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::Cognito::IdentityPoolRoleAttachment', {
        "IdentityPoolId": {
            "Ref": "IdentityPool"
        },
        "Roles": {
            "authenticated": {
                "Fn::GetAtt": [
                    "OpenSearchHealthCognitoCognitoopensearchhealthidentitypoolAuthRole50009EF2",
                    "Arn"
                ]
            }
        }
    });

    openSearchDomainStackTemplate.resourceCountIs('AWS::OpenSearchService::Domain', 1);
    openSearchDomainStackTemplate.hasResourceProperties('AWS::OpenSearchService::Domain', {
        "CognitoOptions": {
            "Enabled": true,
            "IdentityPoolId": {
                "Ref": "IdentityPool"
            },
            "RoleArn": {
                "Fn::GetAtt": [
                    "OpenSearchHealthCognitoAmazonOpenSearchServiceCognitoAccessA34D822B",
                    "Arn"
                ]
            },
            "UserPoolId": {
                "Ref": "UserPool"
            }
        }
    });
});
