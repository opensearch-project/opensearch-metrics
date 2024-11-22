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

interface eventDataLakeSnsProps extends SnsMonitorsProps {
    readonly eventDataLakeSnsAlarms: Array<{ alertName: string }>;
}

export class EventDataLakeSns extends SnsMonitors {
    private readonly eventDataLakeSnsAlarms: Array<{ alertName: string }>;
    constructor(scope: Construct, id: string, props: eventDataLakeSnsProps) {
        super(scope, id, props);
        this.eventDataLakeSnsAlarms = props.eventDataLakeSnsAlarms;
        this.eventDataLakeSnsAlarms.forEach(({ alertName }) => {
            const alarm = this.eventDataLakeAppFailed(alertName);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private eventDataLakeAppFailed(alertName: string): [Alarm, string] {
        const metricPeriod = Duration.minutes(10);

        const eventDataLakeAppFailedMetric = new Metric({
            namespace: this.alarmNameSpace,
            metricName: "LabelCanaryEvent",
            statistic: "Sum",
            period: metricPeriod,
        });

        const filledEventDataLakeAppFailedMetric = new MathExpression({
            expression: "FILL(metric, 0)",
            usingMetrics: {
                metric: eventDataLakeAppFailedMetric,
            },
            period: metricPeriod,
        });

        const alarmObject = new Alarm(this, `error_alarm_${alertName}`, {
            metric: filledEventDataLakeAppFailedMetric,
            threshold: 1,
            evaluationPeriods: 1,
            comparisonOperator: ComparisonOperator.LESS_THAN_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: TreatMissingData.BREACHING,
            alarmDescription: "Detect GitHub Event Data Lake App failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}

