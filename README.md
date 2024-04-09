![OpenSearch logo](./OpenSearch.svg)

## OpenSearch Project Metrics

- Generate the project jar by running `./gradlew clean build `, this will also generate a zip with all dependency jars.
- Now `cd infrastructure/`, update the enum `lib/enums/account.ts` file with the desired AWS account and run `deploy` to create all the required backend resources.
  - `cdk deploy OpenSearchHealth-VPC`: To deploy the VPC resources.
  - `cdk deploy OpenSearchHealth-OpenSearch`: To deploy the OpenSearch cluster.
  - `cdk deploy OpenSearchMetrics-Workflow`: To deploy the lambda and step function.
  - `cdk deploy OpenSearchMetrics-HostedZone`: T0 deploy the route53 and DNS setup.
  - `cdk deploy OpenSearchMetricsNginxReadonly`: To deploy the dashboard read only setup.
  - `cdk deploy OpenSearchWAF`: To deploy the AWS WAF for the project ALB's.


## OpenSearch Project Supported Metrics

### General Metrics

| metric_name                         | metric_description                                                                                                         |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Closed Issues                       | The metric that counts the total number of issues that have been closed.                                                   |
| Created Issues                      | The metric that counts the issues created.                                                                                 |
| Issue Comments                      | The metric that counts the total number of comments made on issues.                                                        |
| Negative Reactions                  | The metric that counts the reactions such as 👎 or similar GitHub negative indicators.                                      |
| Positive Reactions                  | The metric that counts the reactions such as 👍 or similar GitHub positive indicators.                                                       |
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

| metric_name                                 | metric_description                                                                                |
|---------------------------------------------|---------------------------------------------------------------------------------------------------|
| Issues Grouped by Label                     | The metric that counts the issues associated with a specific label.                               |
| Pull Requests Grouped by Label              | The metric that counts the pull requests associated with a specific label.                        |

### Release Metrics

| metric_name               | metric_description                                                                           |
|---------------------------|----------------------------------------------------------------------------------------------|
| Release state             | The metric that shows the state of a release (open or closed).                               |
| Release Branch            | The metric that finds if the release branch is create or not.                                |
| Version Increment         | The metric that finds if the version increment is done or not.                               |
| Release Notes             | The metric that finds if the release notes is created or not.                                |
| Open AutoCut Issues       | The metric that counts the created AUTOCUT issues.                                           |
| Issues with Release Label | The metric that counts the issues (open and closed state) that has the release label.        |                                                                         |
| Pull Requests with Release Label | The metric that counts the pull requests (open and merged state) that has the release label. |                                                                           |

### OpenSearch Core Metrics

| metric_name        | metric_description                 |
|--------------------|------------------------------------|
| Contributions      | The GitHub Contributions by Userid |
| Contribution Goals | Goals for number of contributions  |
| Flaky tests        | Open Issues with label `Flaky`       |


### OpenSearch Gradle Check Metrics

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

