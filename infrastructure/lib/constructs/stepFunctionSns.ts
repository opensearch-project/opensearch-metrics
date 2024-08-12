/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Alarm, ComparisonOperator, Metric, TreatMissingData } from "aws-cdk-lib/aws-cloudwatch";
import { Construct } from "constructs";
import { SnsMonitors, SnsMonitorsProps } from "./snsMonitor";

interface stepFunctionSnsProps extends SnsMonitorsProps {
    readonly stepFunctionSnsAlarms: Array<{ alertName: string, stateMachineName: string }>;
}

export class StepFunctionSns extends SnsMonitors {
    private readonly stepFunctionSnsAlarms: Array<{ alertName: string, stateMachineName: string }>;
    constructor(scope: Construct, id: string, props: stepFunctionSnsProps) {
        super(scope, id, props);
        this.stepFunctionSnsAlarms = props.stepFunctionSnsAlarms;
        this.stepFunctionSnsAlarms.forEach(({ alertName, stateMachineName }) => {
            const alarm = this.stepFunctionExecutionsFailed(alertName, stateMachineName);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private stepFunctionExecutionsFailed(alertName: string, stateMachineName: string): [Alarm, string] {
        const alarmObject = new Alarm(this, `error_alarm_${alertName}`, {
            metric: new Metric({
                namespace: this.alarmNameSpace,
                metricName: "ExecutionsFailed",
                statistic: "Sum",
                dimensionsMap: {
                    StateMachineArn: `arn:aws:states:${this.region}:${this.accountId}:stateMachine:${stateMachineName}`
                }
            }),
            threshold: 1,
            evaluationPeriods: 1,
            comparisonOperator: ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: TreatMissingData.NOT_BREACHING,
            alarmDescription: "Detect SF execution failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}

