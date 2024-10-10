package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.opensearchmetrics.dagger.DaggerServiceComponent;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.model.event.EventData;
import org.opensearchmetrics.util.OpenSearchUtil;
import org.opensearchmetrics.util.S3Util;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GithubEventsLambda extends AbstractBaseLambda {
    private final String[] eventsToIndex = {"issues.opened",
            "issues.closed",
            "issues.labeled",
            "issues.unlabeled",
            "issues.transferred",
            "issues.assigned",
            "issue_comment.created",
            "pull_request.closed",
            "pull_request.opened",
            "pull_request.labeled",
            "pull_request.unlabeled",
            "pull_request.assigned",
            "pull_request_review.submitted",
            "pull_request_review_comment.created",
            "gollum"};
    private final String bucketName = System.getenv("EVENT_BUCKET_NAME");
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    private final OpenSearchUtil openSearchUtil;
    private final S3Util s3Util;
    private final ObjectMapper mapper;

    public GithubEventsLambda() {
        this(COMPONENT.getOpenSearchUtil(), COMPONENT.getS3Util(), COMPONENT.getObjectMapper());
    }

    @VisibleForTesting
    GithubEventsLambda(@NonNull OpenSearchUtil openSearchUtil, @NonNull S3Util s3Util, @NonNull ObjectMapper mapper) {
        this.openSearchUtil = openSearchUtil;
        this.s3Util = s3Util;
        this.mapper = mapper;
    }

    @Override
    public Void handleRequest(Void input, Context context) {
        final String yesterday = LocalDate.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS).toString(); // yyyy-MM-dd
        Map<String, String> finalEventData = new HashMap<>();
        for (String eventToIndex : eventsToIndex) {
            String prefix = eventToIndex + "/" + yesterday + "/";
            List<String> objectKeys = s3Util.listObjectsKeys(prefix);
            for (String objectKey : objectKeys) {
                try (ResponseInputStream<GetObjectResponse> eventInputStream = s3Util.getObjectInputStream(objectKey)) {
                    JsonNode eventNode = mapper.readTree(eventInputStream);
                    EventData event = new EventData();
                    event.setId(eventNode.path("id").textValue());
                    event.setType(eventNode.path("name").textValue());
                    event.setRepository(eventNode.path("payload").path("repository").path("name").textValue());
                    event.setOrganization(eventNode.path("payload").path("organization").path("login").textValue());
                    if (event.getOrganization() == null) {
                        event.setOrganization(eventNode.path("payload").path("repository").path("owner").path("login").textValue());
                    }
                    event.setAction(eventNode.path("payload").path("action").textValue());
                    event.setSender(eventNode.path("payload").path("sender").path("login").textValue());
                    event.setCreatedAt(eventNode.path("uploaded_at").textValue());

                    finalEventData.put(event.getId(), event.getJson(event, mapper));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        openSearchUtil.createIndexIfNotExists("github-events");
        openSearchUtil.bulkIndex("github-events", finalEventData);
        return null;
    }
}
