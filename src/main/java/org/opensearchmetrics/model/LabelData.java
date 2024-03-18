package org.opensearchmetrics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public final class LabelData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;

    @JsonProperty("repository")
    private String repository;

    @JsonProperty("label_name")
    private String labelName;

    @JsonProperty("label_issue_count")
    @JsonSerialize(using = CustomLongSerializer.class)
    private Long labelIssueCount;

    @JsonProperty("label_pull_count")
    @JsonSerialize(using = CustomLongSerializer.class)
    private Long labelPullCount;

    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("label_name", labelName);
        data.put("label_issue_count", labelIssueCount);
        data.put("label_pull_count", labelPullCount);
        return mapper.writeValueAsString(data);
    }

    public String getJson(LabelData labelData, ObjectMapper objectMapper) {
        try {
            return labelData.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " +  e);
            throw new RuntimeException(e);
        }
    }
}
