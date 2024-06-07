enum Project{
    AWS_ACCOUNT = '',
    JENKINS_MASTER_ROLE = '',
    JENKINS_AGENT_ROLE = '',
    REGION = '',
    METRICS_HOSTED_ZONE = 'metrics.opensearch.org',
    // The METRICS_COGNITO_HOSTED_ZONE is s login endpoint for the OpenSearch Dashboards, update as required to allow OpenSearch Dashboards to login using AWS Cognito
    METRICS_COGNITO_HOSTED_ZONE = 'sample.login.endpoint',
    RESTRICTED_PREFIX = '',
    LAMBDA_PACKAGE = 'opensearch-metrics-1.0.zip',
    EC2_AMI_SSM = '',
    SNS_ALERT_EMAIL = 'insert@test.mail'
}
export default Project;
