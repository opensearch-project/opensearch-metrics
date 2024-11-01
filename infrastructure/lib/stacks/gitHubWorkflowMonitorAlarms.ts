/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Duration, Stack } from "aws-cdk-lib";
import { Alarm, ComparisonOperator, Metric } from "aws-cdk-lib/aws-cloudwatch";
import { Construct } from "constructs";

export interface AlarmProps {
    readonly namespace: string;
    readonly metricName: string;
    readonly workflows: string[];
}

export class GitHubWorkflowMonitorAlarms extends Stack {
    readonly workflowAlarmsArn: string[] = [];
    readonly metricName: string
    constructor(scope: Construct, id: string, props: AlarmProps) {
        super(scope, id);
        props.workflows.forEach(workflow => {
            const dimensionValue = workflow;
            const workflowMetric = new Metric({
                namespace: props.namespace,
                metricName: props.metricName,
                dimensionsMap: {
                    Workflow: dimensionValue,
                },
                period: Duration.minutes(5),
                statistic: 'Sum',
            });

            const alarm = new Alarm(this, `OpenSearchMetrics-GitHubApp-${dimensionValue.replace(/\s+/g, '')}-FailuresAlarm`, {
                alarmName: `OpenSearchMetrics-GitHubApp-${dimensionValue.replace(/\s+/g, '')}-FailuresAlarm`,
                metric: workflowMetric,
                threshold: 2,
                evaluationPeriods: 1,
                comparisonOperator: ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
                alarmDescription: `Alarm for ${workflow} failures`,
                actionsEnabled: true,
            });
            this.workflowAlarmsArn.push(alarm.alarmArn)
        });
    }
}
