/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.model.release;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ReleaseMetricsData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;

    @JsonProperty("repository")
    private String repository;

    @JsonProperty("component")
    private String component;

    @JsonProperty("release_version")
    private String releaseVersion;

    @JsonProperty("version")
    private String version;

    @JsonProperty("release_state")
    private String releaseState;

    @JsonProperty("issues_open")
    private Long issuesOpen;

    @JsonProperty("autocut_issues_open")
    private Long autocutIssuesOpen;

    @JsonProperty("issues_closed")
    private Long issuesClosed;

    @JsonProperty("pulls_open")
    private Long pullsOpen;

    @JsonProperty("pulls_closed")
    private Long pullsClosed;

    @JsonProperty("version_increment")
    private boolean versionIncrement;

    @JsonProperty("release_notes")
    private boolean releaseNotes;

    @JsonProperty("release_branch")
    private boolean releaseBranch;

    @JsonProperty("release_owners")
    private String[] releaseOwners;

    @JsonProperty("release_owner_exists")
    private boolean releaseOwnerExists;

    @JsonProperty("release_issue")
    private String releaseIssue;

    @JsonProperty("release_issue_exists")
    private boolean releaseIssueExists;

    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("component", component);
        data.put("release_version", releaseVersion);
        data.put("version", version);
        data.put("release_state", releaseState);
        data.put("issues_open", issuesOpen);
        data.put("autocut_issues_open", autocutIssuesOpen);
        data.put("issues_closed", issuesClosed);
        data.put("pulls_open", pullsOpen);
        data.put("pulls_closed", pullsClosed);
        data.put("version_increment", versionIncrement);
        data.put("release_notes", releaseNotes);
        data.put("release_branch", releaseBranch);
        data.put("release_owners", releaseOwners);
        data.put("release_owner_exists", releaseOwnerExists);
        data.put("release_issue", releaseIssue);
        data.put("release_issue_exists", releaseIssueExists);

        return mapper.writeValueAsString(data);
    }

    public String getJson(ReleaseMetricsData releaseMetricsData, ObjectMapper objectMapper) {
        try {
            return releaseMetricsData.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " +  e);
            throw new RuntimeException(e);
        }
    }
}
