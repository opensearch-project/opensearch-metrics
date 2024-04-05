import {RemovalPolicy, Stack} from "aws-cdk-lib";
import { Construct } from "constructs";
import * as opensearch from "aws-cdk-lib/aws-opensearchservice";
import { Domain } from "aws-cdk-lib/aws-opensearchservice";
import * as iam from "aws-cdk-lib/aws-iam";
import { ArnPrincipal, CompositePrincipal, Effect, IRole, ManagedPolicy, PolicyDocument, PolicyStatement, Role, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { VpcStack } from "./vpc";
import {OpenSearchMetricsCognito} from "../constructs/opensearchCognito";
import {OpenSearchMetricsNginxCognito} from "../constructs/opensearchNginxProxyCognito";;


export interface OpenSearchStackProps {
    readonly region: string;
    readonly account: string;
    readonly vpcStack: VpcStack;
    readonly enableNginxCognito: boolean;
    readonly jenkinsAccess?: jenkinsAccess;
}


export interface jenkinsAccess {
    jenkinsAccountRoles: iam.IPrincipal[]
}


export interface OpenSearchDomainConfig {
    domainName: string
    ebsOptions: {
        volumeSize: number
    }
}

export class OpenSearchDomainStack extends Stack {
    public readonly domain: Domain;
    public readonly props: OpenSearchStackProps;
    public readonly fullAccessRole: IRole;
    public readonly openSearchLambdaRole: IRole;
    public readonly opensearchDomainConfig: OpenSearchDomainConfig;

    constructor(scope: Construct, id: string, props: OpenSearchStackProps) {
        super(scope, id);
        this.props = props;


        this.openSearchLambdaRole = new Role(this, 'OpenSearchDomainLambdaRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: "OpenSearch Metrics Lambda Execution Role",
            roleName: "OpenSearchLambdaRole",

            inlinePolicies: {
                "opensearchAssumeRolePolicy": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: iam.Effect.ALLOW,
                            actions: ["sts:AssumeRole"],
                            resources: [`arn:aws:iam::${props.account}:role/OpenSearchFullAccessRole`],
                            conditions: {
                                StringEquals: { 'aws:PrincipalAccount': props.account, 'aws:RequestedRegion': props.region,},
                            }
                        })
                    ]
                })
            },
            managedPolicies: [
                ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'),
                ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaVPCAccessExecutionRole'),
            ]
        });

        this.opensearchDomainConfig = {
            domainName: 'opensearch-health',
            ebsOptions: {
                volumeSize: 100
            }
        }

        const domainArn = `arn:aws:es:${props.region}:${props.account}:domain/${this.opensearchDomainConfig.domainName}/*`;

        const secureRolesList = [this.openSearchLambdaRole]
        this.fullAccessRole = new Role(this, 'OpenSearchFullAccessRole', {
            assumedBy: new CompositePrincipal(...secureRolesList.map((role) => new iam.ArnPrincipal(role.roleArn))),
            description: "Master role for OpenSearch full access",
            // The Name used in openSearchLambdaRole
            roleName: "OpenSearchFullAccessRole",
            inlinePolicies: {
                "opensearchFullAccess": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: iam.Effect.ALLOW,
                            actions: ["es:*"],
                            resources: [domainArn]
                        })
                    ]
                })
            }
        });

        const metricsCognito = new OpenSearchMetricsCognito(this, "OpenSearchHealthCognito", {
            openSearchDomainArn: domainArn
        });


        const clusterAccessPolicy = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ["es:ESHttp*"],
            principals: [
                new ArnPrincipal(
                    metricsCognito.identityPoolAuthRole.roleArn
                ),
                new ArnPrincipal(
                    this.fullAccessRole.roleArn
                )
            ],
            resources: [domainArn]
        })

        if(props.jenkinsAccess) {
            const jenkinsAccessRole = new Role(this, 'OpenSearchJenkinsAccessRole', {
                assumedBy: new iam.CompositePrincipal(...props.jenkinsAccess.jenkinsAccountRoles),
                description: "Role to Allow OpenSearch build Jenkins accessing the Cluster",
                roleName: "OpenSearchJenkinsAccessRole",
            });
            clusterAccessPolicy.addPrincipals(new ArnPrincipal(jenkinsAccessRole.roleArn))
        }

        this.domain = new opensearch.Domain(this, 'OpenSearchHealthDomain', {
            version: opensearch.EngineVersion.OPENSEARCH_2_11,
            vpc: props.vpcStack.vpc,
            // vpcSubnets: [props.vpcStack.subnets],
            vpcSubnets: [this.props.vpcStack.subnets],
            securityGroups: props.vpcStack.securityGroup ? [props.vpcStack.securityGroup] : undefined,
            domainName: this.opensearchDomainConfig.domainName,
            enableVersionUpgrade: true,
            enforceHttps: true,
            capacity: {
                masterNodes: 3,
                dataNodes: 6,
                dataNodeInstanceType: "r6g.2xlarge.search",
                masterNodeInstanceType: "m6g.large.search",
            },
            ebs: {
                enabled: true,
                volumeSize: this.opensearchDomainConfig.ebsOptions.volumeSize,
            },
            zoneAwareness: {
                enabled: true
            },
            nodeToNodeEncryption: true,
            encryptionAtRest: {
                enabled: true
            },
            cognitoDashboardsAuth: {
                identityPoolId: metricsCognito.identityPool.ref,
                userPoolId: metricsCognito.userPool.ref,
                role: metricsCognito.metricsCognitoAccessRole
            },
            fineGrainedAccessControl: {
                masterUserArn: metricsCognito.identityPoolAdminRole.roleArn,
            },
            accessPolicies: [clusterAccessPolicy],
            logging: {
                auditLogEnabled: true,
                appLogEnabled: true,
                slowSearchLogEnabled: true,
                slowIndexLogEnabled: true,
            },
            removalPolicy: RemovalPolicy.RETAIN
        });


        const serviceLinkedRole = new iam.CfnServiceLinkedRole(this, 'OpensearchServiceLinkedRole', {
            awsServiceName: 'es.amazonaws.com',
            description: 'Service Role for OpenSearch to access resources in VPC'
        });

        this.domain.node.addDependency(serviceLinkedRole);

        if(props.enableNginxCognito) {
            new OpenSearchMetricsNginxCognito(this, "OpenSearchMetricsNginx", {
                region: this.props.region,
                vpc: props.vpcStack.vpc,
                securityGroup: props.vpcStack.securityGroup,
                opensearchDashboardUrlProps: {
                    opensearchDashboardVpcUrl: this.domain.domainEndpoint,
                    cognitoDomain: metricsCognito.userPoolDomain.domain
                }
            });
        }
    }
}