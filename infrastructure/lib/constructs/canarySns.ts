import {SnsMonitors} from "./snsMonitor";
import {SnsMonitorsProps} from "./snsMonitor";
import {Construct} from "constructs";
import {Alarm} from "aws-cdk-lib/aws-cloudwatch";
import { Canary } from 'aws-cdk-lib/aws-synthetics';
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";

interface canarySnsProps extends SnsMonitorsProps {
    readonly canaryAlarms: Array<{ alertName: string, canary: Canary }>;
}

export class canarySns extends SnsMonitors {
    private readonly canaryAlarms: Array<{ alertName: string, canary: Canary }>;
    constructor(scope: Construct, id: string, props: canarySnsProps) {
        super(scope, id, props);
        this.canaryAlarms = props.canaryAlarms;
        this.canaryAlarms.forEach(({ alertName, canary }) =>
        {
            const alarm = this.canaryFailed(alertName, canary);
            this.map[alarm[1]] = alarm[0];
        });
        this.createTopic();
    }

    private canaryFailed(alertName: string, canary: Canary): [Alarm, string] {
        const alarmObject = new cloudwatch.Alarm(this, `error_alarm_${alertName}`, {
            metric: canary.metricSuccessPercent(),
            threshold: 50,
            evaluationPeriods: 1,
            comparisonOperator: cloudwatch.ComparisonOperator.LESS_THAN_THRESHOLD,
            datapointsToAlarm: 1,
            treatMissingData: cloudwatch.TreatMissingData.NOT_BREACHING,
            alarmDescription: "Detect Canary failure",
            alarmName: alertName,
        });
        return [alarmObject, alertName];
    }
}


