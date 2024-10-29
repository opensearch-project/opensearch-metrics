
/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Stack, StackProps } from "aws-cdk-lib";
import { Peer, Port, SecurityGroup, SelectedSubnets, SubnetType, Vpc } from 'aws-cdk-lib/aws-ec2';
import { Construct } from "constructs";

export class VpcStack extends Stack {
    public readonly vpc: Vpc;
    public readonly securityGroup: SecurityGroup
    public readonly subnets: SelectedSubnets

    constructor(scope: Construct, id: string, props?: StackProps) {
        super(scope, id);
        this.vpc = new Vpc(this, 'OpenSearchHealthVpc');
        this.securityGroup = new SecurityGroup(this, "VpcSecurityGroup", {
            vpc: this.vpc
        });
        this.securityGroup.addIngressRule(Peer.ipv4(this.vpc.vpcCidrBlock), Port.tcp(443), "Allow inbound HTTPS traffic");
        this.subnets = this.vpc.selectSubnets({
            subnetType: SubnetType.PRIVATE_WITH_EGRESS
        })
    }
}
