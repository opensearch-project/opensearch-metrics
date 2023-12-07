![OpenSearch logo](./OpenSearch.svg)

## OpenSearch Project Health

- Generate the project jar by running `./gradlew clean build `, this will also generate a zip with all dependency jars.
- Now `cd infrastructure/`, update the enum `lib/enums/account.ts` file with the desired AWS account and run `cdk deploy OpenSearchHealth-Workflow` to create all the required backend resources.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

