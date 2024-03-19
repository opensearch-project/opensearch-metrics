import {App, CfnOutput, Stack, StackProps} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from "./stacks/vpc";
import { OpenSearchDomainStack } from "./stacks/opensearch";
import Project from './enums/project';
import {OpenSearchHealthRoute53} from "./stacks/route53";
import {OpenSearchMetricsWorkflowStack} from "./stacks/metricsWorkflow";
import {OpenSearchMetricsNginxReadonly} from "./stacks/opensearchNginxProxyReadonly";

// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class InfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    const app = new App();

    // Create VPC for the entire setup
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});


    // Create OpenSearch Domain, roles, permissions, cognito setup
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      vpcStack: vpcStack,
      enableNginxCognito: true,
    });


    // Create OpenSearch Metrics Lambda setup
    const openSearchMetricsWorkflowStack = new OpenSearchMetricsWorkflowStack(app, 'OpenSearchMetrics-Workflow', {
      opensearchDomainStack: openSearchDomainStack, vpcStack: vpcStack, lambdaPackage: Project.LAMBDA_PACKAGE})
    openSearchMetricsWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);


    // Create OpenSearch Metrics Frontend DNS
    const metricsHostedZone = new OpenSearchHealthRoute53(app, "OpenSearchMetrics-HostedZone", {
      hostedZone: Project.METRICS_HOSTED_ZONE,
      appName: "OpenSearchMetrics"
    });

    // Create OpenSearch Metrics Frontend Nginx Server and load balancer
    const openSearchMetricsNginxReadonly = new OpenSearchMetricsNginxReadonly(app, "OpenSearchMetricsNginxReadonly", {
      region: Project.REGION,
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

  }
}
