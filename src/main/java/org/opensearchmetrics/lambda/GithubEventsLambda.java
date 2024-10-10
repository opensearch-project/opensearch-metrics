package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.opensearchmetrics.dagger.DaggerServiceComponent;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.metrics.events.GithubEvents;
import org.opensearchmetrics.model.event.EventData;
import org.opensearchmetrics.util.OpenSearchUtil;
import org.opensearchmetrics.util.S3Util;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GithubEventsLambda implements RequestHandler<Map<String, String>, Void> {
    private final GithubEvents[] eventsToIndex = GithubEvents.getAllGithubEvents();
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
    public Void handleRequest(Map<String, String> input, Context context) {
        LocalDate collectionStartDate; // UTC in the format yyyy-MM-dd

        // Reads Step Function Execution input in the format:
        // {
        //  "collectionStartDate": "yyyy-MM-dd"
        // }
        //
        // If not provided, defaults to yesterday

        if (input.containsKey("collectionStartDate")) { // user manually specified collection start date
            collectionStartDate = LocalDate.parse(input.get("collectionStartDate"));
        } else { // defaults to yesterday
            collectionStartDate = LocalDate.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS);
        }
        LocalDate collectionCurrentDate = collectionStartDate;
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        while (collectionCurrentDate.isBefore(today)) {
            Map<String, String> finalEventData = new HashMap<>();
            for (GithubEvents eventToIndex : eventsToIndex) {
                String prefix = eventToIndex.getEventName() + "/" + collectionCurrentDate + "/";
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
            String indexName = "github-user-activity-events-" + collectionCurrentDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
            openSearchUtil.createIndexIfNotExists(indexName);
            openSearchUtil.bulkIndex(indexName, finalEventData);
            collectionCurrentDate = collectionCurrentDate.plusDays(1);
        }
        return null;
    }
}
