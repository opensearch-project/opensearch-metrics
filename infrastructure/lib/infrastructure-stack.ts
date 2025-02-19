/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App, Fn, Stack, StackProps } from 'aws-cdk-lib';
import { ArnPrincipal } from "aws-cdk-lib/aws-iam";
import { Construct } from 'constructs';
import { OpenSearchMetricsNginxCognito } from "./constructs/opensearchNginxProxyCognito";
import Project from './enums/project';
import { GitHubAutomationApp } from "./stacks/gitHubAutomationApp";
import { OpenSearchMetricsWorkflowStack } from "./stacks/metricsWorkflow";
import { OpenSearchMetricsMonitoringStack } from "./stacks/monitoringDashboard";
import { OpenSearchDomainStack } from "./stacks/opensearch";
import { OpenSearchMetricsNginxReadonly } from "./stacks/opensearchNginxProxyReadonly";
import { OpenSearchHealthRoute53 } from "./stacks/route53";
import { OpenSearchS3 } from "./stacks/s3";
import { OpenSearchMetricsSecretsStack } from "./stacks/secrets";
import { VpcStack } from "./stacks/vpc";
import { OpenSearchWAF } from "./stacks/waf";
import { GitHubWorkflowMonitorAlarms } from "./stacks/gitHubWorkflowMonitorAlarms";
import { OpenSearchS3EventIndexWorkflowStack } from "./stacks/s3EventIndexWorkflow";
import { OpenSearchMaintainerInactivityWorkflowStack } from "./stacks/maintainerInactivityWorkflow";
import {OpenSearchEventCanaryWorkflowStack} from "./stacks/eventCanaryWorkflow";

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

    // Create S3 bucket for the GitHub Events
    const openSearchEventsS3Bucket = new OpenSearchS3(app, "OpenSearchMetrics-GitHubAutomationAppEvents-S3");

    // Create resources to launch the GitHub Automation App
    const gitHubAutomationApp = new GitHubAutomationApp(app, "OpenSearchMetrics-GitHubAutomationApp", {
      vpc: vpcStack.vpc,
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      ami: Project.EC2_AMI_SSM.toString(),
      secret: openSearchMetricsGitHubAutomationAppSecretStack.secret,
      workflowAlarmsArn: gitHubWorkflowMonitorAlarms.workflowAlarmsArn,
      githubEventsBucketArn: openSearchEventsS3Bucket.bucket.bucketArn
    });


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
      githubAutomationAppAccess: gitHubAutomationApp.githubAppRole.roleArn,
      githubEventsBucket: openSearchEventsS3Bucket.bucket,
    });

    // Create OpenSearch Metrics Lambda setup
    const openSearchMetricsWorkflowStack = new OpenSearchMetricsWorkflowStack(app, 'OpenSearchMetrics-Workflow', {
      opensearchDomainStack: openSearchDomainStack,
      vpcStack: vpcStack,
      lambdaPackage: Project.LAMBDA_PACKAGE,
    })
    openSearchMetricsWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

    // Create OpenSearch S3 Event Index Lambda setup
    const openSearchS3EventIndexWorkflowStack = new OpenSearchS3EventIndexWorkflowStack(app, 'OpenSearchS3EventIndex-Workflow', {
      region: Project.REGION,
      opensearchDomainStack: openSearchDomainStack,
      vpcStack: vpcStack,
      lambdaPackage: Project.LAMBDA_PACKAGE,
      githubEventsBucket: openSearchEventsS3Bucket.bucket
    })
    openSearchS3EventIndexWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

    // Create OpenSearch Maintainer Inactivity Lambda setup
    const openSearchMaintainerInactivityWorkflowStack = new OpenSearchMaintainerInactivityWorkflowStack(app, 'OpenSearchMaintainerInactivity-Workflow', {
      opensearchDomainStack: openSearchDomainStack,
      vpcStack: vpcStack,
      lambdaPackage: Project.LAMBDA_PACKAGE,
    })
    openSearchMaintainerInactivityWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

    // Create Secret Manager for the metrics project
    const openSearchMetricsSecretsStack = new OpenSearchMetricsSecretsStack(app, "OpenSearchMetrics-Secrets", {
      secretName: 'metrics-creds'
    });

    // Create OpenSearch Event Canary Lambda setup
    const openSearchEventCanaryWorkflowStack = new OpenSearchEventCanaryWorkflowStack(app, 'OpenSearchEventCanary-Workflow', {
      vpcStack: vpcStack,
      lambdaPackage: Project.LAMBDA_PACKAGE,
      gitHubRepoTarget: Project.EVENT_CANARY_REPO_TARGET,
      gitHubAppSecret: openSearchMetricsSecretsStack.secret,
    })
    openSearchEventCanaryWorkflowStack.node.addDependency(vpcStack);

    // Create Monitoring Dashboard

    const openSearchMetricsMonitoringStack = new OpenSearchMetricsMonitoringStack(app, "OpenSearchMetrics-Monitoring", {
      region: Project.REGION,
      account: Project.AWS_ACCOUNT,
      workflowComponent: {
        opensearchMetricsWorkflowStateMachineName: openSearchMetricsWorkflowStack.workflowComponent.opensearchMetricsWorkflowStateMachineName,
        opensearchMaintainerInactivityWorkflowStateMachineName: openSearchMaintainerInactivityWorkflowStack.workflowComponent.opensearchMaintainerInactivityWorkflowStateMachineName,
        opensearchS3EventIndexWorkflowStateMachineName: openSearchS3EventIndexWorkflowStack.workflowComponent.opensearchS3EventIndexWorkflowStateMachineName,
      },
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
