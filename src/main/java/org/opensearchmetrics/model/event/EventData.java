package org.opensearchmetrics.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class EventData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("repository")
    private String repository;

    @JsonProperty("type")
    private String type;

    @JsonProperty("action")
    private String action;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("created_at")
    private String createdAt;

    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("organization", organization);
        data.put("repository", repository);
        data.put("type", type);
        data.put("action", action);
        data.put("sender", sender);
        data.put("created_at", createdAt);
        return mapper.writeValueAsString(data);
    }

    public String getJson(EventData eventData, ObjectMapper objectMapper) {
        try {
            return eventData.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " + e);
            throw new RuntimeException(e);
        }
    }
}
