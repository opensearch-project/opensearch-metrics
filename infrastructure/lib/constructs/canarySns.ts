/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { SnsMonitors } from "./snsMonitor";
import { SnsMonitorsProps } from "./snsMonitor";
import { Construct } from "constructs";
import { Alarm, ComparisonOperator, TreatMissingData } from "aws-cdk-lib/aws-cloudwatch";
import { Canary } from 'aws-cdk-lib/aws-synthetics';
import { Duration } from "aws-cdk-lib";

interface canarySnsProps extends SnsMonitorsProps {
    readonly canaryAlarms: Array<{ alertName: string, canary: Canary }>;
}

export class canarySns extends SnsMonitors {
    private readonly canaryAlarms: Array<{ alertName: string, canary: Canary }>;
    constructor(scope: Construct, id: string, props: canarySnsProps) {
        super(scope, id, props);
        this.canaryAlarms = props.canaryAlarms;
        this.canaryAlarms.forEach(({ alertName, canary }) => {
            const alarm = this.canaryFailed(alertName, canary);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private canaryFailed(alertName: string, canary: Canary): [Alarm, string] {
        const alarmObject = new Alarm(this, `error_alarm_${alertName}`, {
            metric: canary.metricSuccessPercent({
                period: Duration.minutes(15)
            }),
            threshold: 0,
            evaluationPeriods: 1,
            comparisonOperator: ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: TreatMissingData.NOT_BREACHING,
            alarmDescription: "Detect Canary failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}


