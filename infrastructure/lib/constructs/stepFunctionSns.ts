import {SnsMonitors} from "./snsMonitor";
import {SnsMonitorsProps} from "./snsMonitor";
import {Construct} from "constructs";
import {Alarm} from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";

interface stepFunctionSnsProps extends SnsMonitorsProps {
    readonly stepFunctionSnsAlarms: Array<{ alertName: string, stateMachineName: string }>;
}

export class StepFunctionSns extends SnsMonitors {
    private readonly stepFunctionSnsAlarms: Array<{ alertName: string, stateMachineName: string }>;
    constructor(scope: Construct, id: string, props: stepFunctionSnsProps) {
        super(scope, id, props);
        this.stepFunctionSnsAlarms = props.stepFunctionSnsAlarms;
        this.stepFunctionSnsAlarms.forEach(({ alertName, stateMachineName }) =>
        {
            const alarm = this.stepFunctionExecutionsFailed(alertName, stateMachineName);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private stepFunctionExecutionsFailed(alertName: string, stateMachineName: string): [Alarm, string] {
        const alarmObject = new cloudwatch.Alarm(this, `error_alarm_${alertName}`, {
            metric: new cloudwatch.Metric({
                namespace:  this.alarmNameSpace,
                metricName: "ExecutionsFailed",
                statistic: "Sum",
                dimensionsMap: {
                    StateMachineArn: `arn:aws:states:${this.region}:${this.accountId}:stateMachine:${stateMachineName}`
                }
            }),
            threshold: 1,
            evaluationPeriods: 1,
            comparisonOperator: cloudwatch.ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
            alarmDescription: "Detect SF execution failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}

