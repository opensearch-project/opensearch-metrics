import { Construct } from 'constructs';
import * as sns from "aws-cdk-lib/aws-sns";
import * as subscriptions from "aws-cdk-lib/aws-sns-subscriptions";
import * as actions from "aws-cdk-lib/aws-cloudwatch-actions";
import {OpenSearchLambda} from "./lambda";
import Project from '../enums/project';


export interface SnsMonitorsProps {
    readonly region: string;
    readonly accountId: string;
    readonly alarmNameSpace: string;
    readonly snsTopicName: string;
    readonly slackLambda: OpenSearchLambda;
}

export class SnsMonitors extends Construct {
    protected readonly region: string;
    protected readonly accountId: string;
    protected readonly alarmNameSpace: string;
    protected readonly map: { [id: string]: any };
    private readonly snsTopicName: string;
    private readonly slackLambda: OpenSearchLambda;
    private readonly emailList: Array<string>;


    constructor(scope: Construct, id: string, props: SnsMonitorsProps) {
        super(scope, id);
        this.region = props.region;
        this.accountId = props.accountId;
        this.alarmNameSpace = props.alarmNameSpace;
        this.snsTopicName = props.snsTopicName;
        this.slackLambda = props.slackLambda;

        // The email list for receiving alerts
        this.emailList = [
            Project.SNS_ALERT_EMAIL
        ];

        // Create alarms
        this.map = {};

    }

    protected createTopic(){
        // Create SNS topic for alarms to be sent to
        const snsTopic = new sns.Topic(this, `OpenSearchMetrics-Alarm-${this.snsTopicName}`, {
            displayName: `OpenSearchMetrics-Alarm-${this.snsTopicName}`
        });

        // Iterate map to create SNS topic and add alarms on it
        Object.keys(this.map).map(key => {
            // Connect the alarm to the SNS
            this.map[key].addAlarmAction(new actions.SnsAction(snsTopic));
        })

        // Send email notification to the recipients
        for (const email of this.emailList) {
            snsTopic.addSubscription(new subscriptions.EmailSubscription(email));
        }

        // Send slack notification
        snsTopic.addSubscription(new subscriptions.LambdaSubscription(this.slackLambda.lambda));
    }
}
