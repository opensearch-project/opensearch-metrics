import {Stack, StackProps} from "aws-cdk-lib";
import {Construct} from "constructs";
import { CfnWebACL, CfnWebACLAssociation, CfnWebACLAssociationProps } from 'aws-cdk-lib/aws-wafv2';
import {ApplicationLoadBalancer, NetworkLoadBalancer} from "aws-cdk-lib/aws-elasticloadbalancingv2";

interface WafRule {
    name: string;
    rule: CfnWebACL.RuleProperty;
}

const awsManagedRules: WafRule[] = [
    // AWS IP Reputation list includes known malicious actors/bots and is regularly updated
    {
        name: 'AWS-AWSManagedRulesAmazonIpReputationList',
        rule: {
            name: 'AWS-AWSManagedRulesAmazonIpReputationList',
            priority: 0,
            statement: {
                managedRuleGroupStatement: {
                    vendorName: 'AWS',
                    name: 'AWSManagedRulesAmazonIpReputationList',
                },
            },
            overrideAction: {
                none: {},
            },
            visibilityConfig: {
                sampledRequestsEnabled: true,
                cloudWatchMetricsEnabled: true,
                metricName: 'AWSManagedRulesAmazonIpReputationList',
            },
        },
    },
    // Blocks common SQL Injection
    {
        name: 'AWS-AWSManagedRulesSQLiRuleSet',
        rule: {
            name: 'AWS-AWSManagedRulesSQLiRuleSet',
            priority: 1,
            statement: {
                managedRuleGroupStatement: {
                    vendorName: 'AWS',
                    name: 'AWSManagedRulesSQLiRuleSet',
                    excludedRules: [],
                },
            },
            visibilityConfig: {
                sampledRequestsEnabled: true,
                cloudWatchMetricsEnabled: true,
                metricName: 'AWS-AWSManagedRulesSQLiRuleSet',
            },
            overrideAction: {
                none: {},
            },
        },
    },
    // Block request patterns associated with the exploitation of vulnerabilities specific to WordPress sites.
    {
        name: 'AWS-AWSManagedRulesWordPressRuleSet',
        rule: {
            name: 'AWS-AWSManagedRulesWordPressRuleSet',
            priority: 2,
            visibilityConfig: {
                sampledRequestsEnabled: true,
                cloudWatchMetricsEnabled: true,
                metricName: 'AWS-AWSManagedRulesWordPressRuleSet',
            },
            overrideAction: {
                none: {},
            },
            statement: {
                managedRuleGroupStatement: {
                    vendorName: 'AWS',
                    name: 'AWSManagedRulesWordPressRuleSet',
                    excludedRules: [],
                },
            },
        },
    },
];

export class WAF extends CfnWebACL {
    constructor(scope: Construct, id: string) {
        super(scope, id, {
            defaultAction: { allow: {} },
            visibilityConfig: {
                cloudWatchMetricsEnabled: true,
                metricName: 'OpenSearchWAF-WAF',
                sampledRequestsEnabled: true,
            },
            scope: 'REGIONAL',
            name: 'OpenSearchWAF-WAF',
            rules: awsManagedRules.map((wafRule) => wafRule.rule),
        });
    }
}

export class WebACLAssociation extends CfnWebACLAssociation {
    constructor(scope: Construct, id: string, props: CfnWebACLAssociationProps) {
        super(scope, id, {
            resourceArn: props.resourceArn,
            webAclArn: props.webAclArn,
        });
    }
}

export interface WafProps extends StackProps{
    loadBalancer: ApplicationLoadBalancer,
    appName: string
}

export class OpenSearchWAF extends Construct {
    constructor(scope: Construct, id: string, props: WafProps) {
        super(scope, id);
        const waf = new WAF(this, `${props.appName}-WAFv2`);
        new WebACLAssociation(this, 'wafALBassociation', {
            resourceArn: props.loadBalancer.loadBalancerArn,
            webAclArn: waf.attrArn,
        });
    }
}