/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.maintainer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MaintainerData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;

    @JsonProperty("repository")
    private String repository;

    @JsonProperty("name")
    private String name;

    @JsonProperty("github_login")
    private String githubLogin;

    @JsonProperty("affiliation")
    private String affiliation;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("event_action")
    private String eventAction;

    @JsonProperty("time_last_engaged")
    private String timeLastEngaged;

    @JsonProperty("inactive")
    private boolean inactive;

    public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("name", name);
        data.put("github_login", githubLogin);
        data.put("affiliation", affiliation);
        data.put("event_type", eventType);
        data.put("event_action", eventAction);
        data.put("time_last_engaged", timeLastEngaged);
        data.put("inactive", inactive);
        return mapper.writeValueAsString(data);
    }

    public String getJson(MaintainerData maintainerData, ObjectMapper objectMapper) {
        try {
            return maintainerData.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            System.out.println("Error while serializing ReportDataRow to JSON " + e);
            throw new RuntimeException(e);
        }
    }
}


