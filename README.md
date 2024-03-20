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

| metric_name                         | metric_description                                                                                                         |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Closed Issues                       | This metric simply counts the total number of issues that have been closed.                                                |
| Created Issues                      | Metrics related to created issues on GitHub.                                                                               |
| Issue Comments                      | This metric counts the total number of comments made on issues.                                                            |
| Negative Reactions                  | Look for reactions such as 👎 or similar GitHub negative indicators.                                                       |
| Positive Reactions                  | Look for reactions such as 👍 or similar GitHub positive indicators.                                                       |
| Merged Pull Requests                | The metric that counts the total number of pull requests that have been successfully merged.                               |
| Open Issues                         | The metric that counts the total number of issues that are currently open and unresolved.                                  |
| Open Pull Requests                  | The metric that counts the total number of pull requests that are currently open and awaiting review and merge.            |
| Pull Requests comments              | The metric that counts the number of open pull requests that have not received any comments from reviewers or maintainers. |
| Uncommented Pull Requests           | The metric that counts the total number of open pull requests that haven't received any comments.                          |
| Unlabelled Issues                   | The metric that counts the total number of issues that do not have any labels assigned to them.                            |
| Unlabelled Pull Requests            | The metric that counts the total number of pull requests that do not have any labels assigned to them.                     |
| Untriaged Issues                    | The metric that counts the total number of issues that have triaged or acknowledged.                                       |
| Avgerage: Pull Requests Open/Closed | The metric that determines the duration from the PR creation date to the PR close date.                                    |
| Avgerage: Issues Open/Closed        | The metric that determines the duration from the issue creation date to the issue close date.                              |


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

