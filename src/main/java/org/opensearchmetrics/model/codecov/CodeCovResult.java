/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.model.codecov;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
public class CodeCovResult {

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

    @JsonProperty("release_state")
    private String releaseState;

    @JsonProperty("version")
    private String version;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("commitid")
    private String commitId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("coverage")
    private Double coverage;

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("url")
    private String url;


    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("component", component);
        data.put("release_version", releaseVersion);
        data.put("version", version);
        data.put("release_state", releaseState);
        data.put("commitid", commitId);
        data.put("state", state);
        data.put("coverage", coverage);
        data.put("branch", branch);
        data.put("url", url);
        return mapper.writeValueAsString(data);
    }

    public String getJson(CodeCovResult codeCovResult, ObjectMapper objectMapper) {
        try {
            return codeCovResult.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " +  e);
            throw new RuntimeException(e);
        }
    }
}
