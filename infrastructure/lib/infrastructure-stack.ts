import {App, Fn, Stack, StackProps} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from "./stacks/vpc";
import {jenkinsAccess, OpenSearchDomainStack} from "./stacks/opensearch";
import Project from './enums/project';
import {OpenSearchHealthRoute53} from "./stacks/route53";
import {OpenSearchMetricsWorkflowStack} from "./stacks/metricsWorkflow";
import {OpenSearchMetricsNginxReadonly} from "./stacks/opensearchNginxProxyReadonly";
import {ArnPrincipal, IPrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchWAF} from "./stacks/waf";
import {OpenSearchMetricsNginxCognito} from "./constructs/opensearchNginxProxyCognito";

// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class InfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    const app = new App();

    // Create VPC for the entire setup
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});


    // Create OpenSearch Domain, roles, permissions, cognito setup, cross account OpenSearch access for jenkins
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
      albProps: {
        hostedZone: metricsHostedZone,
        certificateArn: metricsHostedZone.certificateArn,
      },
    });
    openSearchMetricsNginxReadonly.node.addDependency(vpcStack, openSearchDomainStack);

    // Create an OpenSearch WAF stack
    const openSearchWAF = new OpenSearchWAF(app, "OpenSearchWAF", {
      readOnlyLoadBalancerArn: Fn.importValue(`${OpenSearchMetricsNginxReadonly.READONLY_ALB_ARN}`),
      cognitoLoadBalancerArn: Fn.importValue(`${OpenSearchMetricsNginxCognito.COGNITO_ALB_ARN}`),
      appName: "OpenSearchMetricsWAF"
    });
    openSearchWAF.node.addDependency(openSearchDomainStack, openSearchMetricsNginxReadonly);

  }
}
