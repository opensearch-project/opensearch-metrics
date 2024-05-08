[![](https://img.shields.io/codecov/c/gh/opensearch-project/opensearch-metrics)](https://app.codecov.io/gh/opensearch-project/opensearch-metrics)

![OpenSearch logo](./OpenSearch.svg)

## OpenSearch Metrics

The OpenSearch Metrics project showcases and keeps track of several important OpenSearch health metrics, with the goal of presenting a comprehensive overview of the OpenSearch project's health and status to the community through visualizations. The OpenSearch Metrics dashboard is accessible at [URL](https://metrics.opensearch.org/_dashboards/app/dashboards#/list?_g=(filters:!(),refreshInterval:(pause:!t,value:0),time:(from:now-4y,to:now))).

### [OpenSearch Project Insights](https://metrics.opensearch.org/_dashboards/app/dashboards#/view/a987a4b0-d801-11ee-8a84-e3710560950c)

| metric_name                         | metric_description                                                                                                         |
|-------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| Closed Issues                       | The metric that counts the total number of issues that have been closed.                                                   |
| Created Issues                      | The metric that counts the issues created.                                                                                 |
| Issue Comments                      | The metric that counts the total number of comments made on issues.                                                        |
| Negative Reactions                  | The metric that counts the reactions such as 👎 or similar GitHub negative indicators.                                     |
| Positive Reactions                  | The metric that counts the reactions such as 👍 or similar GitHub positive indicators.                                     |
| Merged Pull Requests                | The metric that counts the total number of pull requests that have been successfully merged.                               |
| Open Issues                         | The metric that counts the total number of issues that are currently open and unresolved.                                  |
| Open Pull Requests                  | The metric that counts the total number of pull requests that are currently open and awaiting review and merge.            |
| Pull Requests comments              | The metric that counts the number of open pull requests that have not received any comments from reviewers or maintainers. |
| Uncommented Pull Requests           | The metric that counts the total number of open pull requests that haven't received any comments.                          |
| Unlabelled Issues                   | The metric that counts the total number of issues that do not have any labels assigned to them.                            |
| Unlabelled Pull Requests            | The metric that counts the total number of pull requests that do not have any labels assigned to them.                     |
| Untriaged Issues                    | The metric that counts the total number of issues that have not triaged or acknowledged.                                   |
| Avgerage: Pull Requests Open/Closed | The metric that determines the duration from the PR creation date to the PR close date.                                    |
| Avgerage: Issues Open/Closed        | The metric that determines the duration from the issue creation date to the issue close date.                              |
| Issues Grouped by Label             | The metric that counts the issues associated with a specific label.                                                        |
| Pull Requests Grouped by Label      | The metric that counts the pull requests associated with a specific label.                                                 |

### [OpenSearch Release Metrics](https://metrics.opensearch.org/_dashboards/app/dashboards#/view/12d47dd0-e0cc-11ee-86f3-3358a59f8c46)

| metric_name               | metric_description                                                                           |
|---------------------------|----------------------------------------------------------------------------------------------|
| Release state             | The metric that shows the state of a release (open or closed).                               |
| Release Branch            | The metric that finds if the release branch is create or not.                                |
| Version Increment         | The metric that finds if the version increment is done or not.                               |
| Release Notes             | The metric that finds if the release notes is created or not.                                |
| Open AutoCut Issues       | The metric that counts the created AUTOCUT issues.                                           |
| Issues with Release Label | The metric that counts the issues (open and closed state) that has the release label.        |                                                                         |
| Pull Requests with Release Label | The metric that counts the pull requests (open and merged state) that has the release label. |                                                                           |

### [OpenSearch Ops Metrics](https://metrics.opensearch.org/_dashboards/app/dashboards#/view/f1ad21c0-e323-11ee-9a74-07cd3b4ff414)

| metric_name                         | metric_description                                                                                                  |
|-------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Contributions                       | The GitHub Contributions by Userid.                                                                                 |
| Untriaged Issues older than 2 weeks | The metric that counts the total number of issues that have not triaged or acknowledged and are older than 2 weeks. |
| Pulls Merged Weekly                 | The number of Pull Requests merged per week.                                                                        |
| Open Pull Requests                  | The number of Open Pull Requests, Open Pull Requests created within the last two weeks, and number of Draft PRs.    |    
| Issues Created                      | Issues created on a particular date.                                                                                |   
| Open Pull Requests by age           | A data table of Pull Requests and their data.                                                                       | 
| Flaky tests data                    | Flaky tests breakdown by repo.                                                                                      | 
| Flaky tests count                   | Cumulative count of Flaky tests.                                                                                    |
| Total Comments Weekly               | The total number of comments each week.                                                                             |
| Issue Stats                         | Stats about issues including contributors, Avg days to close, Positive and Negative Reactions data.                 |
| Reactions                           | Positive vs Negative reactions.                                                                                     |
Open Issues with label `Flaky`

### [OpenSearch Gradle Check Metrics](https://metrics.opensearch.org/_dashboards/app/dashboards#/view/e5e64d40-ed31-11ee-be99-69d1dbc75083)

| metric_name                         | metric_description                                     |
|-------------------------------------|--------------------------------------------------------|
| Test failure report                 | Report for Gradle Check failed tests.                  |
| Top test failures                   | Gradle Check top failed tests..                        |
| Class level test failures           | Gradle Check class level test failures.                |
| Gradle failure count at PR level    | Gradle Check test failures data at pull request level. |


## Contributing

See [developer guide](DEVELOPER_GUIDE.md) and [how to contribute to this project](CONTRIBUTING.md).

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

