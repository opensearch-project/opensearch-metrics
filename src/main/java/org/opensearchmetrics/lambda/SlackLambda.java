package org.opensearchmetrics.lambda;

import org.opensearchmetrics.util.SecretsManagerUtil;
import org.opensearchmetrics.datasource.DataSourceType;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.dagger.DaggerServiceComponent;

import java.io.IOException;

@Slf4j
public class SlackLambda implements RequestHandler<SNSEvent, Void> {
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    private final SecretsManagerUtil secretsManagerUtil;

    public SlackLambda() {
        this(COMPONENT.getSecretsManagerUtil());
    }

    @VisibleForTesting
    SlackLambda(@NonNull SecretsManagerUtil secretsManagerUtil) {
        this.secretsManagerUtil = secretsManagerUtil;
    }

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        String slackWebhookURL;
        String slackChannel;
        String slackUsername;
        try {
            slackWebhookURL = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_WEBHOOK_URL).get();
            slackChannel = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_CHANNEL).get();
            slackUsername = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_USERNAME).get();
        } catch (Exception ex) {
            log.error("Unable to get Slack credentials", ex);
            throw new RuntimeException(ex);
        }
        String message = event.getRecords().get(0).getSNS().getMessage();
        sendMessageToSlack(message, slackWebhookURL, slackChannel, slackUsername);
        return null;
    }

    private void sendMessageToSlack(String message, String slackWebhookURL, String slackChannel, String slackUsername) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("channel", slackChannel);
        payload.put("username", slackUsername);
        payload.put("Content", message);
        payload.put("icon_emoji", "");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(slackWebhookURL);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(payload), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);

            System.out.println("{" +
                    "\"message\": \"" + message + "\"," +
                    "\"status_code\": " + response.getStatusLine().getStatusCode() + "," +
                    "\"response\": \"" + response.getEntity().getContent().toString() + "\"" +
                    "}");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}