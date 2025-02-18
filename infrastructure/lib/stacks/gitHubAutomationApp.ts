/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

import { Aspects, Duration, Stack, Tag, Tags } from 'aws-cdk-lib';
import {
    AutoScalingGroup,
    BlockDeviceVolume,
    CfnLaunchConfiguration,
    HealthCheck,
    UpdatePolicy
} from 'aws-cdk-lib/aws-autoscaling';
import {
    InstanceClass,
    InstanceSize,
    InstanceType,
    MachineImage,
    SubnetType,
    Vpc
} from 'aws-cdk-lib/aws-ec2';
import {
    ArnPrincipal,
    CompositePrincipal,
    Effect,
    ManagedPolicy,
    PolicyStatement,
    Role,
    ServicePrincipal
} from 'aws-cdk-lib/aws-iam';
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Construct } from 'constructs';


export interface GitHubAppProps {
    readonly vpc: Vpc;
    readonly region: string;
    readonly account: string;
    readonly ami?: string
    readonly secret: Secret;
    readonly workflowAlarmsArn: string[];
    readonly githubEventsBucketArn: string;
}


export class GitHubAutomationApp extends Stack {

    readonly asg: AutoScalingGroup;
    readonly githubAppRole: Role;

    constructor(scope: Construct, id: string, props: GitHubAppProps) {
        super(scope, id);

        const instanceRole = this.createInstanceRole(props.secret.secretArn, props.account, props.workflowAlarmsArn, props.githubEventsBucketArn);
        this.githubAppRole = instanceRole;

        this.asg = new AutoScalingGroup(this, 'OpenSearchMetrics-GitHubAutomationApp-Asg', {
            instanceType: InstanceType.of(InstanceClass.M5, InstanceSize.XLARGE),
            blockDevices: [{ deviceName: '/dev/xvda', volume: BlockDeviceVolume.ebs(20) }],
            healthCheck: HealthCheck.ec2({ grace: Duration.seconds(90) }),
            machineImage: props && props.ami ?
                MachineImage.fromSsmParameter(props.ami) :
                MachineImage.latestAmazonLinux2(),
            associatePublicIpAddress: false,
            allowAllOutbound: true,
            desiredCapacity: 1,
            minCapacity: 1,
            vpc: props.vpc,
            vpcSubnets: {
                subnetType: SubnetType.PRIVATE_WITH_EGRESS
            },
            role: instanceRole,
            updatePolicy: UpdatePolicy.replacingUpdate()
        });
        Tags.of(this.asg).add("Name", "OpenSearchMetrics-GitHubAutomationApp")


        const launchConfiguration = this.asg.node.findChild('LaunchConfig') as CfnLaunchConfiguration;
        launchConfiguration.metadataOptions = {
            httpPutResponseHopLimit: 2,
            httpEndpoint: "enabled",
            httpTokens: "required"
        };

        const instanceName = 'OpenSearchMetrics-GitHubAutomationApp-Host';
        Aspects.of(this.asg).add(new Tag('name', instanceName, {
            applyToLaunchedInstances: true,
            includeResourceTypes: ['AWS::AutoScaling::AutoScalingGroup']
        }),);
        this.asg.addUserData(...this.getUserData(props.secret.secretName));
    }

    private createInstanceRole(secretArn: string, account: string, alarmsArn: string[], githubEventsBucketArn: string): Role {
        const role = new Role(this, "OpenSearchMetrics-GitHubAutomationApp-Role", {
            assumedBy: new CompositePrincipal(
                new ServicePrincipal('ec2.amazonaws.com'),
                new ArnPrincipal(`arn:aws:iam::${account}:role/OpenSearchMetrics-GitHubAutomationApp-Role`)
            ),
            roleName: "OpenSearchMetrics-GitHubAutomationApp-Role",
        });
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'));
        role.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["secretsmanager:GetSecretValue"],
                resources: [secretArn],
            }),
        );
        role.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["sts:AssumeRole"],
                resources: [role.roleArn],
            }),
        );
        role.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: [
                    "cloudwatch:PutMetricAlarm",
                    "cloudwatch:DescribeAlarms",
                    "cloudwatch:SetAlarmState",
                    "cloudwatch:PutMetricData"
                ],
                resources: alarmsArn,
            }),
        );
        role.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: [
                    "cloudwatch:PutMetricData",
                ],
                resources: ["*"],

            }),
        );
        role.addToPolicy(
            new PolicyStatement({
                effect: Effect.ALLOW,
                actions: [
                    "s3:PutObject",
                ],
                resources: [`${githubEventsBucketArn}/*`],
            }),
        );
        return role;
    }


    private getUserData(secretName: string): string[] {
        return [
            'sudo yum install -y https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/linux_amd64/amazon-ssm-agent.rpm',
            'sudo dnf update -y',
            'sudo yum install git docker -y',
            'sudo systemctl enable docker',
            'sudo systemctl start docker',
            'sudo curl -L https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/sbin/docker-compose',
            'sudo chmod a+x /usr/local/sbin/docker-compose',
            'git clone https://github.com/opensearch-project/automation-app.git --branch 0.3.6',
            `aws secretsmanager get-secret-value --secret-id ${secretName} --query SecretString --output text >> automation-app/.env`,
            'cd automation-app/docker',
            'PORT=8080 RESOURCE_CONFIG=configs/resources/opensearch-project-resource.yml OPERATION_CONFIG=configs/operations/github-merged-pulls-monitor.yml docker-compose -p github-merged-pulls-monitor up -d',
            'PORT=8081 RESOURCE_CONFIG=configs/resources/opensearch-project-resource.yml OPERATION_CONFIG=configs/operations/github-workflow-runs-monitor.yml docker-compose -p github-workflow-runs-monitor up -d',
            'PORT=8082 RESOURCE_CONFIG=configs/resources/opensearch-project-only-org.yml OPERATION_CONFIG=configs/operations/github-events-to-s3.yml docker-compose -p github-events-to-s3 up -d',
        ];
    }
}
