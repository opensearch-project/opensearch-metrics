/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import {Alarm, ComparisonOperator, MathExpression, Metric, TreatMissingData} from "aws-cdk-lib/aws-cloudwatch";
import { Construct } from "constructs";
import { SnsMonitors, SnsMonitorsProps } from "./snsMonitor";
import {Duration} from "aws-cdk-lib";

interface automationAppSnsProps extends SnsMonitorsProps {
    readonly automationAppSnsAlarms: Array<{ alertName: string, metricName: string}>;
}

export class AutomationAppSns extends SnsMonitors {
    private readonly automationAppSnsAlarms: Array<{ alertName: string, metricName: string}>;
    constructor(scope: Construct, id: string, props: automationAppSnsProps) {
        super(scope, id, props);
        this.automationAppSnsAlarms = props.automationAppSnsAlarms;
        this.automationAppSnsAlarms.forEach(({ alertName, metricName }) => {
            const alarm = this.automationAppFailed(alertName, metricName);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private automationAppFailed(alertName: string, metricName: string): [Alarm, string] {
        const metricPeriod = Duration.minutes(10);

        const automationAppFailedMetric = new Metric({
            namespace: this.alarmNameSpace,
            metricName: metricName,
            statistic: "Sum",
            period: metricPeriod,
        });

        const filledAutomationAppFailedMetric = new MathExpression({
            expression: "FILL(metric, 0)",
            usingMetrics: {
                metric: automationAppFailedMetric,
            },
            period: metricPeriod,
        });

        const alarmObject = new Alarm(this, `error_alarm_${alertName}`, {
            metric: filledAutomationAppFailedMetric,
            threshold: 1,
            evaluationPeriods: 1,
            comparisonOperator: ComparisonOperator.LESS_THAN_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: TreatMissingData.BREACHING,
            alarmDescription: "Detect GitHub Automation App failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}

