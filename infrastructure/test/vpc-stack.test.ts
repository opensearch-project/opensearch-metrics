/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

// example test. To run these tests, uncomment this file along with the
// example resource in lib/infrastructure-stack.ts
import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { VpcStack } from "../lib/stacks/vpc";

test('VPC Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'Test-OpenSearchHealth-VPC', {});
    const vpcStackTemplate = Template.fromStack(vpcStack);
    vpcStackTemplate.resourceCountIs('AWS::EC2::VPC', 1);
    vpcStackTemplate.resourceCountIs('AWS::EC2::Subnet', 4);
    vpcStackTemplate.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupEgress": [
            {
                "CidrIp": "0.0.0.0/0",
                "Description": "Allow all outbound traffic by default",
                "IpProtocol": "-1"
            }
        ]
    });
    vpcStackTemplate.hasResourceProperties('AWS::EC2::SecurityGroup', {
        "SecurityGroupIngress": [
            {
                "Description": "Allow inbound HTTPS traffic",
                "FromPort": 443,
                "IpProtocol": "tcp",
                "ToPort": 443
            }
        ]
    });

});
