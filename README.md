![OpenSearch logo](./OpenSearch.svg)

## OpenSearch Project Metrics

- Generate the project jar by running `./gradlew clean build `, this will also generate a zip with all dependency jars.
- Now `cd infrastructure/`, update the enum `lib/enums/account.ts` file with the desired AWS account and run `deploy` to create all the required backend resources.
  - `cdk deploy OpenSearchHealth-VPC`: To deploy the VPC resources.
  - `cdk deploy OpenSearchHealth-OpenSearch`: To deploy the OpenSearch cluster.
  - `cdk deploy OpenSearchMetrics-Workflow`: To deploy the lambda and step function.
  - `cdk deploy OpenSearchMetrics-HostedZone`: T0 deploy the route53 and DNS setup.
  - `cdk deploy OpenSearchMetricsNginxReadonly`: To deploy the dashboard read only setup.


## OpenSearch Project Supported Metrics

### General Metrics

| metric_name                        | metric_description |
|------------------------------------|--------------------|
| Closed Issues                      |                    |
| Created Issues                     |                    |
| Issue Comments                     |                    |
| Negative Reactions                 |                    |
| Positive Reactions                 |                    |
| Merged Pull Requests               |                    |
| Open Issues                        |                    |
| Open Pull Requests                 |                    |
| Pull Requests comments             |                    |
| Uncommented Pull Requests          |                    |
| Unlabelled Issues                  |                    |
| Unlabelled Pull Requests           |                    |
| Untriaged Issues                   |                    |
| Avgerag: Pull Requests Open/Closed |                    |
| Avgerag: Issues Open/Closed        |                    |


### GitHub Label Metrics

| metric_name                                 | metric_description |
|---------------------------------------------|--------------------|
| Issues Grouped by Label                     |                    |
| Pull Requests Grouped by Label              |                    |
| Total Number of Open Issues by label        |                    |
| Total Number of Open Pull Requests by label |                    |

### Release Metrics

| metric_name                  | metric_description |
|------------------------------|--------------------|
| Release state                |                    |
| Release Branch               |                    |
| Version Increment            |                    |
| Release Notes                |                    |
| Open AutoCut Issues          |                    |
| Open Issues with Release Label |                    |
| Closed Issues with Release Label |                    |
| Open Pull Requests with Release Label |                    |
| Closed Pull Requests with Release Label |                    |

### OpenSearch Core Metrics

| metric_name      | metric_description |
|------------------|--------------------|
|                  |                    |
|                  |                    |
|                  |                    |

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

