import {App, Stack, StackProps} from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { VpcStack } from "./stacks/vpc";
import { OpenSearchDomainStack } from "./stacks/opensearch";
import { OpenSearchHealthWorkflowStack } from './stacks/workflow';
import Region from './enums/region';
// import * as sqs from 'aws-cdk-lib/aws-sqs';
export class InfrastructureStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);
    const app = new App();
    const appName = 'OpenSearchHealth';

    const vpcStack = new VpcStack(app, `${appName}-VPC`, {});
    const openSearchDomainStack = new OpenSearchDomainStack(app, `${appName}-OpenSearch`, {
      region: Region.IAD,
      account: "979020455945",
      vpcStack: vpcStack,
    })
    const openSearchHealthWorkflowStack = new OpenSearchHealthWorkflowStack(app, `${appName}-Workflow`, {
      opensearchDomainStack: openSearchDomainStack, vpcStack: vpcStack
    })
    openSearchHealthWorkflowStack.node.addDependency(vpcStack, openSearchDomainStack);

  }
}
