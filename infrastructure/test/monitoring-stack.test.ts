import {App} from "aws-cdk-lib";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchMetricsWorkflowStack} from "../lib/stacks/metricsWorkflow";
import Project from "../lib/enums/project";
import {OpenSearchDomainStack} from "../lib/stacks/opensearch";
import {VpcStack} from "../lib/stacks/vpc";
import {ArnPrincipal} from "aws-cdk-lib/aws-iam";
import {OpenSearchMetricsMonitoringStack} from "../lib/stacks/monitoringDashboard";
import {OpenSearchMetricsSecrets} from "../lib/stacks/secrets";

test('Monitoring Stack Test', () => {
    const app = new App();
    const vpcStack = new VpcStack(app, 'OpenSearchHealth-VPC', {});
    const openSearchMetricsWorkflowStack = new OpenSearchMetricsWorkflowStack(app, 'OpenSearchMetrics-Workflow', {
        opensearchDomainStack: new OpenSearchDomainStack(app, 'Test-OpenSearchHealth-OpenSearch', {
            region: "us-east-1",
            account: "test-account",
            vpcStack: vpcStack,
            enableNginxCognito: true,
            jenkinsAccess: {
                jenkinsAccountRoles: [
                    new ArnPrincipal(Project.JENKINS_MASTER_ROLE),
                    new ArnPrincipal(Project.JENKINS_AGENT_ROLE)
                ]
            }
        }),
        vpcStack: vpcStack,
        lambdaPackage: Project.LAMBDA_PACKAGE
    });
    const openSearchMetricsSecretsStack = new OpenSearchMetricsSecrets(app, "OpenSearchMetrics-Secrets", {
        secretName: 'metrics-creds'
    });
    const openSearchMetricsMonitoringStack = new OpenSearchMetricsMonitoringStack(app, "OpenSearchMetrics-Monitoring", {
        region: Project.REGION,
        account: Project.AWS_ACCOUNT,
        workflowComponent: openSearchMetricsWorkflowStack.workflowComponent,
        lambdaPackage: Project.LAMBDA_PACKAGE,
        secrets: openSearchMetricsSecretsStack.secret,
        vpcStack: vpcStack
    });
    const template = Template.fromStack(openSearchMetricsMonitoringStack);
    template.resourceCountIs('AWS::IAM::Role', 2);
    template.resourceCountIs('AWS::IAM::Policy', 1);
    template.resourceCountIs('AWS::CloudWatch::Alarm', 2);
    template.resourceCountIs('AWS::SNS::Topic', 2);
    template.resourceCountIs('AWS::Synthetics::Canary', 1);
    template.hasResourceProperties('AWS::IAM::Role', {
        "AssumeRolePolicyDocument": {
            "Statement": [
                {
                    "Action": "sts:AssumeRole",
                    "Effect": "Allow",
                    "Principal": {
                        "Service": "lambda.amazonaws.com"
                    }
                }
            ],
            "Version": "2012-10-17"
        },
        "RoleName": "OpenSearchSlackLambdaRole"
    });

    template.hasResourceProperties('AWS::IAM::Policy', {
        "PolicyDocument": {
            "Statement": [
                {
                    "Action": "secretsmanager:GetSecretValue",
                    "Effect": "Allow"
                },
                {
                    "Action": [
                        "xray:PutTraceSegments",
                        "xray:PutTelemetryRecords"
                    ],
                    "Effect": "Allow",
                    "Resource": "*"
                }
            ],
            "Version": "2012-10-17",
        },
        "PolicyName": "OpenSearchSlackLambdaRoleDefaultPolicy849E8281",
        "Roles": [
            {
                "Ref": "OpenSearchSlackLambdaRole441FAD2D"
            }
        ]
    });
    template.hasResourceProperties('AWS::Lambda::Function', {
        "FunctionName": "OpenSearchMetricsDashboardsSlackLambdaLambda",
        "Handler": "org.opensearchmetrics.lambda.SlackLambda",
        "MemorySize": 1024,
        "Role": {
            "Fn::GetAtt": [
                "OpenSearchSlackLambdaRole441FAD2D",
                "Arn"
            ]
        },
        "Runtime": "java17",
        "Timeout": 900,
        "TracingConfig": {
            "Mode": "Active"
        }
    });
    template.hasResourceProperties('AWS::Lambda::Permission', {
        "Action": "lambda:InvokeFunction",
        "FunctionName": {
            "Fn::GetAtt": [
                "OpenSearchMetricsDashboardsSlackLambdaLambda28DA56CA",
                "Arn"
            ]
        },
        "Principal": "sns.amazonaws.com",
        "SourceArn": {
            "Ref": "SnsMonitorsStepFunctionExecutionsFailedOpenSearchMetricsAlarmStepFunctionExecutionsFailed0B259DBC"
        }
    });
    template.hasResourceProperties('AWS::CloudWatch::Alarm', {
        "AlarmActions": [
            {
                "Ref": "SnsMonitorsStepFunctionExecutionsFailedOpenSearchMetricsAlarmStepFunctionExecutionsFailed0B259DBC"
            }
        ],
        "AlarmDescription": "Detect SF execution failure",
        "AlarmName": "StepFunction_execution_errors_MetricsWorkflow",
        "ComparisonOperator": "GreaterThanOrEqualToThreshold",
        "DatapointsToAlarm": 1,
        "Dimensions": [
            {
                "Name": "StateMachineArn",
                "Value": {
                    "Fn::Join": [
                        "",
                        [
                            "arn:aws:states:::stateMachine:",
                            {
                                "Fn::ImportValue": "OpenSearchMetrics-Workflow:ExportsOutputFnGetAttOpenSearchMetricsWorkflowDB4D4CB1NameE4E75A02"
                            }
                        ]
                    ]
                }
            }
        ],
        "EvaluationPeriods": 1,
        "MetricName": "ExecutionsFailed",
        "Namespace": "AWS/States",
        "Period": 300,
        "Statistic": "Sum",
        "Threshold": 1,
        "TreatMissingData": "notBreaching"
    });
    template.hasResourceProperties('AWS::Synthetics::Canary', {
        "ArtifactS3Location": {
            "Fn::Join": [
                "",
                [
                    "s3://",
                    {
                        "Ref": "CanaryHeartbeatMonitorArtifactsBucketA8125411"
                    }
                ]
            ]
        },
        "Code": {
            "Handler": "urlMonitor.handler",
            "S3Bucket": {
                "Fn::Sub": "cdk-hnb659fds-assets-${AWS::AccountId}-${AWS::Region}"
            },
            "S3Key": "3add60a2b13650e2ad0c97ef8b24082c52ea91e59b8b8bac89874c602a6b908d.zip"
        },
        "ExecutionRoleArn": {
            "Fn::GetAtt": [
                "CanaryHeartbeatMonitorServiceRole424026C0",
                "Arn"
            ]
        },
        "Name": "metrics_heartbeat",
        "RunConfig": {
            "EnvironmentVariables": {
                "SITE_URL": "https://metrics.opensearch.org"
            }
        },
        "RuntimeVersion": "syn-nodejs-puppeteer-7.0",
        "Schedule": {
            "DurationInSeconds": "0",
            "Expression": "rate(1 minute)"
        },
        "StartCanaryAfterCreation": true,
        "VPCConfig": {
            "SecurityGroupIds": [
                {
                    "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputFnGetAttVpcSecurityGroup092B7291GroupIdA3F0A2EB"
                }
            ],
            "SubnetIds": [
                {
                    "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcPrivateSubnet1Subnet529349B600974078"
                },
                {
                    "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcPrivateSubnet2SubnetBA599EDB2BEEEA30"
                }
            ],
            "VpcId": {
                "Fn::ImportValue": "OpenSearchHealth-VPC:ExportsOutputRefOpenSearchHealthVpcB885AABED860B3EB"
            }
        }
    });
    template.hasResourceProperties('AWS::CloudWatch::Alarm', {
        "AlarmActions": [
            {
                "Ref": "SnsMonitorsCanaryFailedOpenSearchMetricsAlarmCanaryFailed4CF8A950"
            }
        ],
        "AlarmDescription": "Detect Canary failure",
        "AlarmName": "Canary_failed_MetricsWorkflow",
        "ComparisonOperator": "LessThanThreshold",
        "DatapointsToAlarm": 1,
        "Dimensions": [
            {
                "Name": "CanaryName",
                "Value": {
                    "Ref": "CanaryHeartbeatMonitorFE4C06BE"
                }
            }
        ],
        "EvaluationPeriods": 1,
        "MetricName": "SuccessPercent",
        "Namespace": "CloudWatchSynthetics",
        "Period": 300,
        "Statistic": "Average",
        "Threshold": 100,
        "TreatMissingData": "notBreaching"
    });
});
