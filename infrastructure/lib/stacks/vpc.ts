
import {IpAddresses, Peer, Port, SecurityGroup, SelectedSubnets, Subnet, SubnetType, Vpc} from 'aws-cdk-lib/aws-ec2';
import {CfnOutput, Stack, StackProps} from "aws-cdk-lib";
import {Construct} from "constructs";


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
            subnetType: SubnetType.PRIVATE_WITH_EGRESS})
    }
}
