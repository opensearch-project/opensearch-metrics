import {App} from "aws-cdk-lib";
import {Template} from "aws-cdk-lib/assertions";
import {OpenSearchMetricsSecretsStack} from "../lib/stacks/secrets";

test('Secrets Stack Test', () => {
    const app = new App();
    const openSearchMetricsSecretsStack = new OpenSearchMetricsSecretsStack(app, "OpenSearchMetrics-Secrets", {
        secretName: 'metrics-creds'
    });
    const template = Template.fromStack(openSearchMetricsSecretsStack);
    template.resourceCountIs('AWS::SecretsManager::Secret', 1);
    template.hasResourceProperties('AWS::SecretsManager::Secret', {
        "GenerateSecretString": {},
        "Name": "metrics-creds"
    });
});
