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

- Now `cd infrastructure/`, update the enum `lib/enums/account.ts` file with the desired AWS account and run `deploy` to create all the required backend resources.
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

### Forking and Cloning

Fork this repository on GitHub, and clone locally with `git clone`.

### Submitting Changes

See [CONTRIBUTING](CONTRIBUTING.md).
