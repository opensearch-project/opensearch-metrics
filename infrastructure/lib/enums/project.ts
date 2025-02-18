/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

enum Project {
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
    SNS_ALERT_EMAIL = 'insert@test.mail',
    EVENT_CANARY_REPO_TARGET = '',
}
export default Project;
