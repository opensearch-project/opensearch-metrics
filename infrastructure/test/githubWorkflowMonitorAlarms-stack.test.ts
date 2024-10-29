/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Match, Template } from "aws-cdk-lib/assertions";
import { GitHubWorkflowMonitorAlarms } from "../lib/stacks/gitHubWorkflowMonitorAlarms";

test('OpenSearch Workflow Monitor Alarms test ', () => {
    const app = new App();

    const gitHubWorkflowMonitorAlarms = new GitHubWorkflowMonitorAlarms(app, "Test-OpenSearchMetrics-GitHubWorkflowMonitor-Alarms", {
        namespace: 'GitHubActions',
        metricName: 'WorkflowRunFailures',
        workflows: [
            'Publish snapshots to Apache Maven repositories',
            'Publish snapshots to maven',
            'Run performance benchmark on pull request',
        ],
    });

    const template = Template.fromStack(gitHubWorkflowMonitorAlarms);


    template.hasResourceProperties('AWS::CloudWatch::Alarm', {
        AlarmName: 'OpenSearchMetrics-GitHubApp-Publishsnapshotstomaven-FailuresAlarm',
        Namespace: 'GitHubActions',
        MetricName: 'WorkflowRunFailures',
        Dimensions: Match.arrayWith([{
            Name: 'Workflow',
            Value: 'Publish snapshots to maven'
        }]),
        ComparisonOperator: 'GreaterThanOrEqualToThreshold',
        EvaluationPeriods: 1,
        Threshold: 2,
        Period: 300,
        Statistic: 'Sum'
    });

    template.hasResourceProperties('AWS::CloudWatch::Alarm', {
        AlarmName: 'OpenSearchMetrics-GitHubApp-Runperformancebenchmarkonpullrequest-FailuresAlarm',
        Namespace: 'GitHubActions',
        MetricName: 'WorkflowRunFailures',
        Dimensions: Match.arrayWith([{
            Name: 'Workflow',
            Value: 'Run performance benchmark on pull request'
        }]),
        ComparisonOperator: 'GreaterThanOrEqualToThreshold',
        EvaluationPeriods: 1,
        Threshold: 2,
        Period: 300,
        Statistic: 'Sum'
    });

});
