package org.opensearchmetrics.lambda;

import org.opensearchmetrics.util.SecretsManagerUtil;
import org.opensearchmetrics.datasource.DataSourceType;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final ObjectMapper mapper;

    public SlackLambda() {
        this(COMPONENT.getSecretsManagerUtil());
    }

    @VisibleForTesting
    SlackLambda(@NonNull SecretsManagerUtil secretsManagerUtil) {
        this.secretsManagerUtil = secretsManagerUtil;
        this.mapper = COMPONENT.getObjectMapper();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class AlarmObject {
        @JsonProperty("AlarmName")
        private String alarmName;
        @JsonProperty("AlarmDescription")
        private String alarmDescription;
        @JsonProperty("StateChangeTime")
        private String stateChangeTime;
        @JsonProperty("Region")
        private String region;
        @JsonProperty("AlarmArn")
        private String alarmArn;
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
        try {
            sendMessageToSlack(message, slackWebhookURL, slackChannel, slackUsername);
        } catch (Exception ex) {
            log.error("Unable to send message to Slack", ex);
            throw new RuntimeException(ex);
        }
        return null;
    }

    private void sendMessageToSlack(String message, String slackWebhookURL, String slackChannel, String slackUsername) throws IOException {
        AlarmObject alarmObject =
                mapper.readValue(message, AlarmObject.class);
        String alarmMessage = ":alert: OpenSearch Metrics Dashboard Monitoring alarm activated. Please investigate the issue. \n" +
                "- Name: " + alarmObject.getAlarmName() + "\n" +
                "- Description: " + alarmObject.getAlarmDescription() + "\n" +
                "- StateChangeTime: " + alarmObject.getStateChangeTime() + "\n" +
                "- Region: " + alarmObject.getRegion() + "\n" +
                "- AlarmArn: " + alarmObject.getAlarmArn();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("channel", slackChannel);
        payload.put("username", slackUsername);
        payload.put("Content", alarmMessage);
        payload.put("icon_emoji", "");

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(slackWebhookURL);
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(payload), "UTF-8"));
            HttpResponse response = httpClient.execute(httpPost);

            System.out.println("{" +
                    "\"message\": \"" + alarmMessage + "\"," +
                    "\"status_code\": " + response.getStatusLine().getStatusCode() + "," +
                    "\"response\": \"" + response.getEntity().getContent().toString() + "\"" +
                    "}");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
