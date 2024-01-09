package org.opensearchhealth.health.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Release {
    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;

    @JsonProperty("release_version")
    private String releaseVersion;

    @JsonProperty("release_issue_number")
    private Long releaseIssueNumber;

    @JsonProperty("release_issue_url")
    private String releaseIssueUrl;

    @JsonProperty("plugins")
    private List<Plugins> plugins;

    @Data
    public static class Plugins {
        @JsonProperty("name")
        private String name;

        @JsonProperty("plugin_release_issue_number")
        private Long pluginReleaseIssueNumber;

        @JsonProperty("plugin_release_issue_url")
        private String pluginReleaseIssueUrl;

        @JsonProperty("release_label_open_issues")
        private Map<String, String> pluginReleaseLabelOpenIssues;
    }

    @Data
    public static class ReleaseIssue {
        @JsonProperty("number")
        private Long number;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("title")
        private String title;

    }

    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("release_version", releaseVersion);
        data.put("release_issue_number", releaseIssueNumber);
        data.put("release_issue_url", releaseIssueUrl);
        data.put("plugins", plugins);
        return mapper.writeValueAsString(data);
    }

    public String getJson(Release release, ObjectMapper objectMapper) {
        try {
            return release.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " +  e);
            throw new RuntimeException(e);
        }
    }

}
