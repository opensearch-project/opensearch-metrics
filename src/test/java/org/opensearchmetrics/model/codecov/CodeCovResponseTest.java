/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.model.codecov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeCovResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new CodeCovDoubleSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void testSerializeNonNullCoverage() throws JsonProcessingException {
        CodeCovResponse response = new CodeCovResponse();
        response.setCommitId("abc123");
        response.setUrl("https://sample.url");
        response.setState("success");
        response.setCoverage(85.5);
        String json = objectMapper.writeValueAsString(response);
        String expectedJson = "{\"commitid\":\"abc123\",\"url\":\"https://sample.url\",\"state\":\"success\",\"coverage\":85.5}";
        assertEquals(expectedJson, json);
    }

    @Test
    public void testSerializeZeroCoverage() throws JsonProcessingException {
        CodeCovResponse response = new CodeCovResponse();
        response.setCommitId("abc123");
        response.setUrl("https://sample.url");
        response.setState("success");
        response.setCoverage(0.0);
        String json = objectMapper.writeValueAsString(response);
        String expectedJson = "{\"commitid\":\"abc123\",\"url\":\"https://sample.url\",\"state\":\"success\",\"coverage\":0.0}";
        assertEquals(expectedJson, json);
    }

    @Test
    public void testDeserializeResponse() throws JsonProcessingException {
        String json = "{\"commitid\":\"abc123\",\"url\":\"https://sample.url\",\"state\":\"success\",\"coverage\":85.5}";
        CodeCovResponse response = objectMapper.readValue(json, CodeCovResponse.class);
        assertEquals("abc123", response.getCommitId());
        assertEquals("https://sample.url", response.getUrl());
        assertEquals("success", response.getState());
        assertEquals(85.5, response.getCoverage());
    }

    @Test
    public void testDeserializeNullCoverage() throws JsonProcessingException {
        String json = "{\"commitid\":\"abc123\",\"url\":\"https://sample.url\",\"state\":\"success\",\"coverage\":null}";
        CodeCovResponse response = objectMapper.readValue(json, CodeCovResponse.class);
        assertEquals("abc123", response.getCommitId());
        assertEquals("https://sample.url", response.getUrl());
        assertEquals("success", response.getState());
        assertEquals(null, response.getCoverage());
    }
}
