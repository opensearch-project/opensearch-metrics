import {Stack} from "aws-cdk-lib";
import { Construct } from 'constructs';
import {Secret} from "aws-cdk-lib/aws-secretsmanager";

export class OpenSearchMetricsSecrets extends Stack {
    readonly secretsObject: Secret;

    constructor(scope: Construct, id: string) {
        super(scope, id);
        this.secretsObject = new Secret(this, 'MetricsCreds', {
            secretName: 'metrics-creds',
        });
    }
}
