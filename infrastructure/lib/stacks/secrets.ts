import {Stack} from "aws-cdk-lib";
import { Construct } from 'constructs';
import {Secret} from "aws-cdk-lib/aws-secretsmanager";

export interface SecretProps {
    readonly secretName: string
}

export class OpenSearchMetricsSecrets extends Stack {
    readonly secret: Secret;

    constructor(scope: Construct, id: string, props: SecretProps ) {
        super(scope, id);
        this.secret = new Secret(this, `MetricsCreds-${props.secretName}`, {
            secretName: props.secretName,
        });
    }
}
