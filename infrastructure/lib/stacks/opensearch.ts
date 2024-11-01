/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { RemovalPolicy, Stack } from "aws-cdk-lib";
import { ArnPrincipal, CfnServiceLinkedRole, CompositePrincipal, Effect, IPrincipal, IRole, ManagedPolicy, PolicyDocument, PolicyStatement, Role, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { Domain, EngineVersion } from "aws-cdk-lib/aws-opensearchservice";
import { Bucket } from "aws-cdk-lib/aws-s3";
import { Construct } from "constructs";
import { OpenSearchMetricsCognito } from "../constructs/opensearchCognito";
import { OpenSearchMetricsNginxCognito } from "../constructs/opensearchNginxProxyCognito";
import Project from "../enums/project";
import { OpenSearchHealthRoute53 } from "./route53";
import { VpcStack } from "./vpc";


export interface OpenSearchStackProps {
    readonly region: string;
    readonly account: string;
    readonly vpcStack: VpcStack;
    readonly enableNginxCognito: boolean;
    readonly jenkinsAccess?: jenkinsAccess;
    readonly githubAutomationAppAccess?: string;
    readonly githubEventsBucket: Bucket;
}


export interface jenkinsAccess {
    jenkinsAccountRoles: IPrincipal[]
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
    public readonly openSearchMetricsLambdaRole: IRole;
    public readonly openSearchS3EventsIndexLambdaRole: IRole;
    public readonly opensearchDomainConfig: OpenSearchDomainConfig;

    constructor(scope: Construct, id: string, props: OpenSearchStackProps) {
        super(scope, id);
        this.props = props;


        this.openSearchMetricsLambdaRole = new Role(this, 'OpenSearchDomainLambdaRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: "OpenSearch Metrics Lambda Execution Role",
            roleName: "OpenSearchLambdaRole",

            inlinePolicies: {
                "opensearchAssumeRolePolicy": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: Effect.ALLOW,
                            actions: ["sts:AssumeRole"],
                            resources: [`arn:aws:iam::${props.account}:role/OpenSearchFullAccessRole`],
                            conditions: {
                                StringEquals: { 'aws:PrincipalAccount': props.account, 'aws:RequestedRegion': props.region, },
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

        this.openSearchS3EventsIndexLambdaRole = new Role(this, 'OpenSearchS3EventIndexLambdaRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: "OpenSearch Metrics S3 Event Index Lambda Execution Role",
            roleName: "OpenSearchS3EventIndexLambdaRole",
            inlinePolicies: {
                "opensearchAssumeRolePolicy": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: Effect.ALLOW,
                            actions: ["sts:AssumeRole"],
                            resources: [`arn:aws:iam::${props.account}:role/OpenSearchFullAccessRole`],
                            conditions: {
                                StringEquals: { 'aws:PrincipalAccount': props.account, 'aws:RequestedRegion': props.region, },
                            }
                        })
                    ]
                }),
                "opensearchReadS3EventsPolicy": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: Effect.ALLOW,
                            actions: ["s3:GetObject",
                                "s3:ListBucket"],
                            resources: [props.githubEventsBucket.bucketArn,
                            `${props.githubEventsBucket.bucketArn}/*`],
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

        const secureRolesList = [this.openSearchMetricsLambdaRole, this.openSearchS3EventsIndexLambdaRole]
        this.fullAccessRole = new Role(this, 'OpenSearchFullAccessRole', {
            assumedBy: new CompositePrincipal(...secureRolesList.map((role) => new ArnPrincipal(role.roleArn))),
            description: "Master role for OpenSearch full access",
            // The Name used in openSearchLambdaRole
            roleName: "OpenSearchFullAccessRole",
            inlinePolicies: {
                "opensearchFullAccess": new PolicyDocument({
                    statements: [
                        new PolicyStatement({
                            effect: Effect.ALLOW,
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

        if (props.jenkinsAccess) {
            const jenkinsAccessRole = new Role(this, 'OpenSearchJenkinsAccessRole', {
                assumedBy: new CompositePrincipal(...props.jenkinsAccess.jenkinsAccountRoles),
                description: "Role to Allow OpenSearch build Jenkins accessing the Cluster",
                roleName: "OpenSearchJenkinsAccessRole",
            });
            clusterAccessPolicy.addPrincipals(new ArnPrincipal(jenkinsAccessRole.roleArn))
        }
        if (props.githubAutomationAppAccess) {
            clusterAccessPolicy.addPrincipals(new ArnPrincipal(props.githubAutomationAppAccess))
        }

        this.domain = new Domain(this, 'OpenSearchHealthDomain', {
            version: EngineVersion.OPENSEARCH_2_13,
            vpc: props.vpcStack.vpc,
            vpcSubnets: [this.props.vpcStack.subnets],
            securityGroups: props.vpcStack.securityGroup ? [props.vpcStack.securityGroup] : undefined,
            domainName: this.opensearchDomainConfig.domainName,
            enableVersionUpgrade: true,
            enforceHttps: true,
            capacity: {
                masterNodes: 3,
                dataNodes: 6,
                dataNodeInstanceType: "r6g.2xlarge.search",
                masterNodeInstanceType: "m6g.xlarge.search",
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


        const serviceLinkedRole = new CfnServiceLinkedRole(this, 'OpensearchServiceLinkedRole', {
            awsServiceName: 'es.amazonaws.com',
            description: 'Service Role for OpenSearch to access resources in VPC'
        });

        this.domain.node.addDependency(serviceLinkedRole);
        if (props.enableNginxCognito) {
            const metricsHostedZone = new OpenSearchHealthRoute53(this, "OpenSearchMetricsCognito-HostedZone", {
                hostedZone: Project.METRICS_COGNITO_HOSTED_ZONE,
                appName: "OpenSearchMetricsCognito"
            });
            new OpenSearchMetricsNginxCognito(this, "OpenSearchMetricsNginx", {
                region: this.props.region,
                vpc: props.vpcStack.vpc,
                securityGroup: props.vpcStack.securityGroup,
                opensearchDashboardUrlProps: {
                    opensearchDashboardVpcUrl: this.domain.domainEndpoint,
                    cognitoDomain: metricsCognito.userPoolDomain.domain
                },
                ami: Project.EC2_AMI_SSM.toString(),
                albProps: {
                    hostedZone: metricsHostedZone,
                    certificateArn: metricsHostedZone.certificateArn,
                },
            });
        }
    }
}
