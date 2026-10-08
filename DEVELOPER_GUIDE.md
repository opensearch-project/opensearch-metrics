- [OpenSearch Project Metrics Developer Guide](#opensearch-project-metrics-developer-guide)
    - [Build](#build)
    - [Deploy](#deploy)
    - [Forking and Cloning](#forking-and-cloning)
    - [Submitting Changes](#submitting-changes)

### OpenSearch Project Metrics Developer Guide

So you want to contribute code to this project? Excellent! We're glad you're here. Here's what you need to do.

#### Build

- Generate the project jar by running `./gradlew clean build `, this will also generate a zip with all dependency jars.

#### Deploy

- Now `cd infrastructure/`, configure the deploy-time values in `lib/enums/project.ts` and run `deploy` to create all the required backend resources.
    - The `lib/enums/project.ts` enum holds all deploy-time configuration:

      | Param | Required | Default | Description |
      |---|---|---|---|
      | `AWS_ACCOUNT` | Yes | `''` | AWS account ID the stacks are deployed into. |
      | `REGION` | Yes | `''` | AWS region to deploy into (e.g. `us-east-1`). |
      | `EC2_AMI_SSM` | Yes | `''` | SSM parameter / AMI ID for the Nginx proxy and GitHub Automation App EC2 hosts. |
      | `RESTRICTED_PREFIX` | Yes | `''` | VPC prefix list ID allowed to reach the Cognito ALB on port 443. |
      | `METRICS_HOSTED_ZONE` | Yes | `metrics.opensearch.org` | Public hosted zone / record name for the metrics frontend. |
      | `METRICS_COGNITO_HOSTED_ZONE` | Yes | `sample.login.endpoint` | Cognito login endpoint hosted zone for OpenSearch Dashboards auth. |
      | `SNS_ALERT_EMAIL` | Yes | `insert@test.mail` | Email address subscribed to the monitoring SNS topic. |
      | `LAMBDA_PACKAGE` | Yes | `opensearch-metrics-1.0.zip` | Filename of the built Lambda deployment package. |
      | `JENKINS_MASTER_ROLE` | No | `''` | External Jenkins master role ARN trusted to assume the CDK-created `OpenSearchJenkinsAccessRole`. |
      | `JENKINS_AGENT_ROLE` | No | `''` | External Jenkins agent role ARN trusted to assume the CDK-created `OpenSearchJenkinsAccessRole`. |
      | `OSCAR_ACCESS_ROLE` | No | `''` | Oscar role ARN added as a principal to the `MetricsDefaultAccess` statement of the domain access policy. |
      | `LINUX_FOUNDATION_ACCESS_ROLE` | No | `''` | Linux Foundation role ARN added as the `LinuxFoundationAccess` statement of the domain access policy, granting `es:ESHttp*` only. |
      | `EVENT_CANARY_REPO_TARGET` | No | `''` | GitHub repository target used by the Event Canary workflow. |

    - `cdk deploy OpenSearchHealth-VPC`: To deploy the VPC resources.
    - `cdk deploy OpenSearchHealth-OpenSearch`: To deploy the OpenSearch cluster.
    - `cdk deploy OpenSearchMetrics-Workflow`: To deploy the lambda and step function.
    - `cdk deploy OpenSearchMetrics-HostedZone`: To deploy the route53 and DNS setup.
    - `cdk deploy OpenSearchMetricsNginxReadonly`: To deploy the dashboard read only setup.
    - `cdk deploy OpenSearchWAF`: To deploy the AWS WAF for the project ALB's.
    - `cdk deploy OpenSearchMetrics-Monitoring`: To deploy the alerting stack which will monitor the step functions and URL of the project coming from [METRICS_HOSTED_ZONE](https://github.com/opensearch-project/opensearch-metrics/blob/main/infrastructure/lib/enums/project.ts)
    - `cdk deploy OpenSearchMetrics-GitHubAutomationApp-Secret`: Creates the GitHub app secret which will be used during the GitHub app runtime.
    - `cdk deploy OpenSearchMetrics-GitHubWorkflowMonitor-Alarms`: Creates the Alarms to Monitor the Critical GitHub CI workflows by the GitHub Automation App.
    - `cdk deploy OpenSearchMetrics-GitHubAutomationApp`: Create the resources which launches the [GitHub Automation App](https://github.com/opensearch-project/automation-app). Listens to GitHub events and index the data to Metrics cluster.
    - `cdk deploy OpenSearchMetrics-GitHubAutomationAppEvents-S3`: Creates the S3 Bucket for the [GitHub Automation App](https://github.com/opensearch-project/automation-app) to store OpenSearch Project GitHub Events.
    - `cdk deploy OpenSearchS3EventIndex-Workflow`: Creates the Lambda and Step Function to index the GitHub Events stored in the S3 Bucket to the Metrics cluster.
    - `cdk deploy OpenSearchMaintainerInactivity-Workflow`: Creates the Lambda and Step Function to index Maintainer Inactivity to the Metrics cluster.
    - `cdk deploy OpenSearchEventCanary-Workflow`: Creates the Lambda and Step Function that runs the GitHub Label Canary for Automation App monitoring.

### Forking and Cloning

Fork this repository on GitHub, and clone locally with `git clone`.

### Submitting Changes

See [CONTRIBUTING](CONTRIBUTING.md).
