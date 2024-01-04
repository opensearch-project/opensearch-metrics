enum Project{
    AWS_TEST = '<AWS_ACCOUNT_NUMBER>',
    IAD = 'us-east-1',
    HOSTED_ZONE = 'insights.opensearch.org',
    // Temp until the project is public
    RESTRICTED_PREFIX = '<SECURITY_GROUP_PRRFIX>',
    // Temp until the repo is public
    FRONTEND_CODE_S3_LOCATION = 's3://<BUCKET_PATH>'
}
export default Project;