/**
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import {Stack, RemovalPolicy, StackProps} from "aws-cdk-lib";
import { Bucket, BlockPublicAccess, BucketEncryption } from 'aws-cdk-lib/aws-s3';
import { Construct } from "constructs";

export class OpenSearchS3 extends Stack {

    public readonly bucket: Bucket;

    constructor(scope: Construct, id: string, props?: StackProps) {
        super(scope, id);

        this.bucket = new Bucket(this, 'OpenSearchS3Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            encryption: BucketEncryption.S3_MANAGED,
            enforceSSL: true,
            versioned: true,
            removalPolicy: RemovalPolicy.RETAIN,
        });
    }
}
