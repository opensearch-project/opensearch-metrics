import {App, CfnOutput, Stack, StackProps} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from "./stacks/vpc";
import { OpenSearchDomainStack } from "./stacks/opensearch";
import { OpenSearchHealthWorkflowStack } from './stacks/workflow';
import Project from './enums/project';
import {OpenSearchHealthFrontendServer} from "./stacks/opensearchFrontendServer";
import {ARecord, HostedZone, RecordTarget} from "aws-cdk-lib/aws-route53";
import {Certificate, CertificateValidation} from "aws-cdk-lib/aws-certificatemanager";
import {OpenSearchHealthRoute53} from "./stacks/route53";
import {OpenSearchMetricsWorkflowStack} from "./stacks/metricsWorkflow";
import {OpenSearchMetricsNginxReadonly} from "./stacks/opensearchNginxProxyReadonly";
import {OpenSearchMetricsNginxJenkins} from "./stacks/opensearchNginxProxyJenkins";

// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class InfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    const app = new App();

    // Create VPC for the entire setup
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});


    // Create OpenSearch Domain, roles, permissions, cognito setup
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
      region: Project.IAD,
      account: Project.AWS_ACCOUNT,
      vpcStack: vpcStack,
      enableNginxCognito: true,
    });

    // Create OpenSearch Health Lambda setup
    const openSearchHealthWorkflowStack = new OpenSearchHealthWorkflowStack(app, 'OpenSearchHealth-Workflow', {
      opensearchDomainStack: openSearchDomainStack, vpcStack: vpcStack
    })
    openSearchHealthWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

    // Create OpenSearch Metrics Lambda setup
    const openSearchMetricsWorkflowStack = new OpenSearchMetricsWorkflowStack(app, 'OpenSearchMetrics-Workflow', {
      opensearchDomainStack: openSearchDomainStack, vpcStack: vpcStack})
    openSearchMetricsWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);




    // Create OpenSearch Health Frontend DNS
    const healthHostedZone = new OpenSearchHealthRoute53(app, "OpenSearchHealth-HostedZone", {
      hostedZone: Project.HEALTH_HOSTED_ZONE,
      appName: "OpenSearchHealth"
    });


    // Create OpenSearch Health Frontend Server and load balancer
    const openSearchHealthFrontendServer = new OpenSearchHealthFrontendServer(app, "OpenSearchHealth-FrontendServer", {
      region: Project.IAD,
      account: Project.AWS_ACCOUNT,
      vpc: vpcStack.vpc,
      opensearchUrl: openSearchDomainStack.domain.domainEndpoint,
      domainName: openSearchDomainStack.domain.domainName,
      nlbProps: {
        hostedZone: healthHostedZone,
        certificateArn: healthHostedZone.certificateArn,
      },
      securityGroup: vpcStack.securityGroup,
    });
    openSearchHealthFrontendServer.node.addDependency(vpcStack, healthHostedZone);


    // Create OpenSearch Metrics Frontend DNS
    const metricsHostedZone = new OpenSearchHealthRoute53(app, "OpenSearchMetrics-HostedZone", {
      hostedZone: Project.METRICS_HOSTED_ZONE,
      appName: "OpenSearchMetrics"
    });

    // Create OpenSearch Metrics Frontend Nginx Server and load balancer
    const openSearchMetricsNginxReadonly = new OpenSearchMetricsNginxReadonly(app, "OpenSearchMetricsNginxReadonly", {
      region: Project.IAD,
      account: Project.AWS_ACCOUNT,
      vpc: vpcStack.vpc,
      securityGroup: vpcStack.securityGroup,
      opensearchDashboardUrlProps: {
        opensearchDashboardVpcUrl: openSearchDomainStack.domain.domainEndpoint,
        openSearchDomainName: openSearchDomainStack.domain.domainName
      },
      nlbProps: {
        hostedZone: metricsHostedZone,
        certificateArn: metricsHostedZone.certificateArn,
      },
    });
    openSearchMetricsNginxReadonly.node.addDependency(vpcStack, openSearchDomainStack);

    // Create OpenSearch Metrics Frontend Nginx Server and load balancer
    const openSearchMetricsNginxJenkins = new OpenSearchMetricsNginxJenkins(app, "OpenSearchMetricsNginxJenkins", {
      region: Project.IAD,
      account: Project.AWS_ACCOUNT,
      vpc: vpcStack.vpc,
      securityGroup: vpcStack.securityGroup,
      opensearchDashboardUrlProps: {
        opensearchDashboardVpcUrl: openSearchDomainStack.domain.domainEndpoint,
        openSearchDomainName: openSearchDomainStack.domain.domainName
      },
    });
    openSearchMetricsNginxReadonly.node.addDependency(vpcStack, openSearchDomainStack);

  }
}
