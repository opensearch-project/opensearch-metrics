import { OpenSearchDomainStack } from "./opensearch";
import { Function } from 'aws-cdk-lib/aws-lambda';
import {OpenSearchLambda} from "../constructs/lambda";
import {Duration, Stack, StackProps} from "aws-cdk-lib";
import { Construct } from 'constructs';
import { VpcStack } from "./vpc";
import {LambdaInvoke} from "aws-cdk-lib/aws-stepfunctions-tasks";
import {JsonPath, Pass, StateMachine, Succeed} from "aws-cdk-lib/aws-stepfunctions";

export interface OpenSearchHealthStackProps extends StackProps {
    readonly opensearchDomainStack: OpenSearchDomainStack;
    readonly vpcStack: VpcStack;
}
export class OpenSearchHealthWorkflowStack extends Stack {
    constructor(scope: Construct, id: string, props: OpenSearchHealthStackProps) {
        super(scope, id, props);

        const healthTask = this.createHealthTask(
            this,
            props.opensearchDomainStack,
            props.vpcStack,
        );
        const opensearchHealthWorkflow = new StateMachine(this, 'OpenSearchHealthWorkflow', {
            definition: healthTask,
            timeout: Duration.minutes(15),
            stateMachineName: 'OpenSearchHealthWorkflow'
        })
    }

    private createHealthTask(scope: Construct, opensearchDomainStack: OpenSearchDomainStack,
                                    vpcStack: VpcStack) {
        const openSearchDomain = opensearchDomainStack.domain;
        const healthLambda = new OpenSearchLambda(scope, "OpenSearchHealthLambdaFunction", {
            lambdaNameBase: "OpenSearchHealth",
            handler: "org.opensearchhealth.lambda.HealthLambda",
            vpc: vpcStack.vpc,
            securityGroup: vpcStack.securityGroup,
            role: opensearchDomainStack.openSearchLambdaRole,
            environment: {
                OPENSEARCH_DOMAIN_ENDPOINT: openSearchDomain.domainEndpoint,
                OPENSEARCH_DOMAIN_REGION: openSearchDomain.env.region,
                OPENSEARCH_DOMAIN_ROLE: opensearchDomainStack.fullAccessRole.roleArn,
            },
        }).lambda;
        return new LambdaInvoke(scope, 'Health Lambda', {
            lambdaFunction: healthLambda,
            resultPath: JsonPath.DISCARD,
            timeout: Duration.minutes(15)
        }).addRetry();
    }
}
