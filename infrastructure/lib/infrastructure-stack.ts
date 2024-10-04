/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App, Fn, Stack, StackProps } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from "./stacks/vpc";
import { OpenSearchDomainStack } from "./stacks/opensearch";
import Project from './enums/project';
import { OpenSearchHealthRoute53 } from "./stacks/route53";
import { OpenSearchMetricsWorkflowStack } from "./stacks/metricsWorkflow";
import { OpenSearchMetricsNginxReadonly } from "./stacks/opensearchNginxProxyReadonly";
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import { OpenSearchWAF } from "./stacks/waf";
import { OpenSearchMetricsNginxCognito } from "./constructs/opensearchNginxProxyCognito";
import { OpenSearchMetricsMonitoringStack } from "./stacks/monitoringDashboard";
import { OpenSearchMetricsSecretsStack } from "./stacks/secrets";
import { GitHubAutomationApp } from "./stacks/gitHubAutomationApp";
import { GitHubWorkflowMonitorAlarms } from "./stacks/gitHubWorkflowMonitorAlarms";

export class InfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    const app = new App();

    // Create VPC for the entire setup
    const vpcStack = new VpcStack(app, "OpenSearchHealth-VPC", {});


    // Create secret related to GitHub Automation App
    const openSearchMetricsGitHubAutomationAppSecretStack = new OpenSearchMetricsSecretsStack(app, "OpenSearchMetrics-GitHubAutomationApp-Secret", {
      secretName: 'opensearch-project-github-automation-app-creds'
    });


   // Alarms to Monitor the Critical GitHub CI workflows by the GitHub Automation App
    const gitHubWorkflowMonitorAlarms = new GitHubWorkflowMonitorAlarms(app, "OpenSearchMetrics-GitHubWorkflowMonitor-Alarms", {
      namespace: 'GitHubActions',
      metricName: 'WorkflowRunFailures',
      workflows: [
        'Publish snapshots to maven',
        'Run performance benchmark on pull request',
      ],
    });

    // Create resources to launch the GitHub Automation App
    const gitHubAutomationApp = new GitHubAutomationApp(app, "OpenSearchMetrics-GitHubAutomationApp", {
      vpc: vpcStack.vpc,
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      ami: Project.EC2_AMI_SSM.toString(),
      secret: openSearchMetricsGitHubAutomationAppSecretStack.secret,
      workflowAlarmsArn: gitHubWorkflowMonitorAlarms.workflowAlarmsArn
    })


    // Create OpenSearch Domain, roles, permissions, cognito setup, cross account OpenSearch access for jenkins
    const openSearchDomainStack = new OpenSearchDomainStack(app, "OpenSearchHealth-OpenSearch", {
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      vpcStack: vpcStack,
      enableNginxCognito: true,
      jenkinsAccess: {
        jenkinsAccountRoles: [
          new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
          new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
        ]
      },
      githubAutomationAppAccess: gitHubAutomationApp.githubAppRole.roleArn
    });

    // Create OpenSearch Metrics Lambda setup
    const openSearchMetricsWorkflowStack = new OpenSearchMetricsWorkflowStack(app, 'OpenSearchMetrics-Workflow', {
      opensearchDomainStack: openSearchDomainStack, vpcStack: vpcStack, lambdaPackage: Project.LAMBDA_PACKAGE
    })
    openSearchMetricsWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

    // Create Secret Manager for the metrics project
    const openSearchMetricsSecretsStack = new OpenSearchMetricsSecretsStack(app, "OpenSearchMetrics-Secrets", {
      secretName: 'metrics-creds'
    });

    // Create Monitoring Dashboard

    const openSearchMetricsMonitoringStack = new OpenSearchMetricsMonitoringStack(app, "OpenSearchMetrics-Monitoring", {
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      workflowComponent: openSearchMetricsWorkflowStack.workflowComponent,
      lambdaPackage: Project.LAMBDA_PACKAGE,
      secrets: openSearchMetricsSecretsStack.secret,
      vpcStack: vpcStack
    });

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
      ami: Project.EC2_AMI_SSM.toString(),
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
