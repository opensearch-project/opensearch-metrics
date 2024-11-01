/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { SnsAction } from 'aws-cdk-lib/aws-cloudwatch-actions';
import { Topic } from 'aws-cdk-lib/aws-sns';
import { EmailSubscription, LambdaSubscription } from 'aws-cdk-lib/aws-sns-subscriptions';
import { Construct } from 'constructs';
import Project from '../enums/project';
import { OpenSearchLambda } from "./lambda";


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

    protected createTopic() {
        // Create SNS topic for alarms to be sent to
        const snsTopic = new Topic(this, `OpenSearchMetrics-Alarm-${this.snsTopicName}`, {
            displayName: `OpenSearchMetrics-Alarm-${this.snsTopicName}`
        });

        // Iterate map to create SNS topic and add alarms on it
        Object.keys(this.map).map(key => {
            // Connect the alarm to the SNS
            this.map[key].addAlarmAction(new SnsAction(snsTopic));
        })

        // Send email notification to the recipients
        for (const email of this.emailList) {
            snsTopic.addSubscription(new EmailSubscription(email));
        }

        // Send slack notification
        snsTopic.addSubscription(new LambdaSubscription(this.slackLambda.lambda));
    }
}
