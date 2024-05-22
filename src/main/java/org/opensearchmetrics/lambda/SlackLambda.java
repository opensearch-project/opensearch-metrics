package org.opensearchmetrics.lambda;

import com.amazon.opensearchmetrics.util.SecretsManagerUtil;
import com.amazon.opensearchmetrics.datasource.DataSourceType;
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

import java.io.IOException;

@Slf4j
public class SlackLambda implements RequestHandler<SNSEvent, Void> {
    try {
        private static final String SLACK_WEBHOOK_URL = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_WEBHOOK_URL).get();
        private static final String SLACK_CHANNEL = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_CHANNEL).get();
        private static final String SLACK_USERNAME = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_USERNAME).get();
    } catch (Exception ex) {
        log.info("Unable to get Slack credentials", ex);
    }

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        String message = event.getRecords().get(0).getSNS().getMessage();
        sendMessageToSlack(message);
        return null;
    }

    private void sendMessageToSlack(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("channel", SLACK_CHANNEL);
        payload.put("username", SLACK_USERNAME);
        payload.put("Content", message);
        payload.put("icon_emoji", "");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(SLACK_WEBHOOK_URL);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(payload), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);

            System.out.println("{" +
                    "\"message\": \"" + message + "\"," +
                    "\"status_code\": " + response.getStatusLine().getStatusCode() + "," +
                    "\"response\": \"" + response.getEntity().getContent().toString() + "\"" +
                    "}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}