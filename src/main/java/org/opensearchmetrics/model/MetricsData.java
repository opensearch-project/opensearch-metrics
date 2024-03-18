package org.opensearchmetrics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MetricsData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;
    @JsonProperty("repository")
    private String repository;

    @JsonProperty("metric_name")
    private String metricName;

    @JsonProperty("metric_count")
    @JsonSerialize(using = CustomLongSerializer.class)
    private Long metricCount;



     public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("metric_name", metricName);
        data.put("metric_count", metricCount);
        return mapper.writeValueAsString(data);
    }

    public String getJson(MetricsData metricsData, ObjectMapper objectMapper) {
        try {
            return metricsData.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " +  e);
            throw new RuntimeException(e);
        }
    }
}

