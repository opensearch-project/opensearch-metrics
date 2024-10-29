/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { OpenSearchS3 } from "../lib/stacks/s3";

test('S3 Stack Test', () => {
    const app = new App();
    const s3Stack = new OpenSearchS3(app, "Test-OpenSearchMetrics-GitHubAutomationAppEvents-S3");
    const s3StackTemplate = Template.fromStack(s3Stack);
    s3StackTemplate.resourceCountIs('AWS::S3::Bucket', 1);
    s3StackTemplate.resourceCountIs('AWS::S3::BucketPolicy', 1);
    s3StackTemplate.hasResourceProperties('AWS::S3::Bucket', {
        "BucketEncryption": {
            "ServerSideEncryptionConfiguration": [
                {
                    "ServerSideEncryptionByDefault": {
                        "SSEAlgorithm": "AES256"
                    }
                }
            ]
        },
        "PublicAccessBlockConfiguration": {
            "BlockPublicAcls": true,
            "BlockPublicPolicy": true,
            "IgnorePublicAcls": true,
            "RestrictPublicBuckets": true
        },
        "VersioningConfiguration": {
            "Status": "Enabled"
        }
    });
    s3StackTemplate.hasResourceProperties('AWS::S3::BucketPolicy', {
        "Bucket": {
            "Ref": "OpenSearchS3Bucket2ED683CC"
        },
        "PolicyDocument": {
            "Statement": [
                {
                    "Action": "s3:*",
                    "Condition": {
                        "Bool": {
                            "aws:SecureTransport": "false"
                        }
                    },
                    "Effect": "Deny",
                    "Principal": {
                        "AWS": "*"
                    },
                    "Resource": [
                        {
                            "Fn::GetAtt": [
                                "OpenSearchS3Bucket2ED683CC",
                                "Arn"
                            ]
                        },
                        {
                            "Fn::Join": [
                                "",
                                [
                                    {
                                        "Fn::GetAtt": [
                                            "OpenSearchS3Bucket2ED683CC",
                                            "Arn"
                                        ]
                                    },
                                    "/*"
                                ]
                            ]
                        }
                    ]
                }
            ],
            "Version": "2012-10-17"
        }
    });

});
