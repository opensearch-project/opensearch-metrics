import { RemovalPolicy } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { Effect, FederatedPrincipal, ManagedPolicy, PolicyStatement, Role, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import * as cognito from "aws-cdk-lib/aws-cognito";

export interface OpenSearchMetricsCognitoProps {
    readonly openSearchDomainArn: string;
}

export class OpenSearchMetricsCognito extends Construct {
    public readonly identityPool: cognito.CfnIdentityPool;
    public readonly userPool: cognito.CfnUserPool;
    public readonly userPoolDomain: cognito.CfnUserPoolDomain;
    public readonly metricsCognitoAccessRole: Role;
    public readonly identityPoolAuthRole: Role;
    public readonly identityPoolAdminRole: Role;


    constructor(scope: Construct, id: string, props: OpenSearchMetricsCognitoProps) {
        super(scope, id);

        const userPoolDomainName = "opensearch-health-user-pool"
        this.userPool = new cognito.CfnUserPool(this, "OpenSearchHealthUserPool", {
            userPoolName: userPoolDomainName,
            adminCreateUserConfig: {
                allowAdminCreateUserOnly: true
            }
        });
        this.userPool.overrideLogicalId("UserPool");
        this.userPool.applyRemovalPolicy(RemovalPolicy.RETAIN);

        const adminGroup = new cognito.CfnUserPoolGroup(this, 'OpensearchAdminGroup', {
            groupName: 'opensearch-admin-group',
            userPoolId: this.userPool.ref,
        });


        const cognitoAppClient = new cognito.CfnUserPoolClient(this, "OpenSearchHealthUserPoolClient", {
            userPoolId: this.userPool.ref,
            clientName: 'Web',
            generateSecret: false,
        });


        this.userPoolDomain = new cognito.CfnUserPoolDomain(this, "OpenSearchHealthUserPoolDomain", {
            userPoolId: this.userPool.ref,
            domain: `${userPoolDomainName}`
        });

        const identityPoolName = "opensearch-health-identity-pool";
        this.identityPool = new cognito.CfnIdentityPool(this, "OpenSearchHealthIdentityPool", {
            identityPoolName: identityPoolName,
            allowUnauthenticatedIdentities: false,
            allowClassicFlow: false,
        });
        this.identityPool.overrideLogicalId("IdentityPool");
        this.identityPool.node.addDependency(cognitoAppClient);


        const admin_roleName = `Cognito_${identityPoolName}_Admin_Role`;
        this.identityPoolAdminRole = new Role(this, admin_roleName, {
            roleName: admin_roleName,
            assumedBy: new FederatedPrincipal(
                "cognito-identity.amazonaws.com",
                {
                    StringEquals: {
                        "cognito-identity.amazonaws.com:aud": this.identityPool.ref

                    },
                    "ForAnyValue:StringLike": {
                        "cognito-identity.amazonaws.com:amr":
                            "authenticated"
                    }
                },
                "sts:AssumeRoleWithWebIdentity"
            )
        });


        const roleName = `Cognito_${identityPoolName}_Auth_Role`;
        this.identityPoolAuthRole = new Role(this, roleName, {
            roleName: roleName,
            assumedBy: new FederatedPrincipal(
                "cognito-identity.amazonaws.com",
                {
                    StringEquals: {
                        "cognito-identity.amazonaws.com:aud": this.identityPool.ref

                    },
                    "ForAnyValue:StringLike": {
                        "cognito-identity.amazonaws.com:amr":
                            "authenticated"
                    }
                },
                "sts:AssumeRoleWithWebIdentity"
            )
        });

        this.identityPoolAuthRole.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["es:ESHttpGet", "es:ESHttpPost"],
                resources: [`${props.openSearchDomainArn}`],
            }),
        );

        this.identityPoolAdminRole.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["es:ESHttp*", ],
                resources: [`${props.openSearchDomainArn}`],
            }),
        );

        new cognito.CfnIdentityPoolRoleAttachment(this, "IdentityPoolRoleAttachment", {
            identityPoolId: this.identityPool.ref,
            roles: {
                authenticated: this.identityPoolAuthRole.roleArn,
            },
        });

        adminGroup.addPropertyOverride('RoleArn', this.identityPoolAdminRole.roleArn)

        this.metricsCognitoAccessRole = new Role(this, "AmazonOpenSearchServiceCognitoAccess", {
            assumedBy: new ServicePrincipal("es.amazonaws.com"),
            managedPolicies: [
                ManagedPolicy.fromAwsManagedPolicyName("AmazonOpenSearchServiceCognitoAccess")
            ]
        });
    }
}
