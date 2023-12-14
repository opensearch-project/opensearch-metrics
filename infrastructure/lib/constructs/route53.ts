import {Stack} from "aws-cdk-lib";
import {Construct} from "constructs";
import {HostedZone} from "aws-cdk-lib/aws-route53";
import {Certificate, CertificateValidation} from "aws-cdk-lib/aws-certificatemanager";

export interface Route53Props {
    readonly hostedZone: string,
}

export class OpenSearchHealthRoute53 extends Stack {
    readonly zone: HostedZone;
    readonly certificateArn: string
    constructor(scope: Construct, id: string, props: Route53Props) {

        super(scope, id);

        this.zone = new HostedZone(this, "OpenSearchHealth-HostedZone", {
            zoneName: props.hostedZone,
        });

        const certificate = new Certificate(this, "OpenSearchHealth-Certificate", {
            domainName: props.hostedZone,
            certificateName: "OpenSearchHealth",
            validation: CertificateValidation.fromDns(this.zone),
        });
        this.certificateArn = certificate.certificateArn;
    }
}