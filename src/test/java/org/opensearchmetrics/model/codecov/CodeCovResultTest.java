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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CodeCovResultTest {

    private ObjectMapper objectMapper;

    private CodeCovResult codeCovResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeCovResult = new CodeCovResult();
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new CodeCovDoubleSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void testId() {
        codeCovResult.setId("123");
        assertEquals("123", codeCovResult.getId());
    }

    @Test
    public void testCurrentDate() {
        codeCovResult.setCurrentDate("2024-01-01");
        assertEquals("2024-01-01", codeCovResult.getCurrentDate());
    }

    @Test
    public void testRepository() {
        codeCovResult.setRepository("test-repo");
        assertEquals("test-repo", codeCovResult.getRepository());
    }

    @Test
    public void testComponent() {
        codeCovResult.setComponent("test-component");
        assertEquals("test-component", codeCovResult.getComponent());
    }

    @Test
    public void testReleaseVersion() {
        codeCovResult.setReleaseVersion("2.0.0");
        assertEquals("2.0.0", codeCovResult.getReleaseVersion());
    }

    @Test
    public void testReleaseState() {
        codeCovResult.setReleaseState("released");
        assertEquals("released", codeCovResult.getReleaseState());
    }

    @Test
    public void testVersion() {
        codeCovResult.setVersion("1.0");
        assertEquals("1.0", codeCovResult.getVersion());
    }

    @Test
    public void testTimestamp() {
        codeCovResult.setTimestamp("2024-01-01T12:00:00Z");
        assertEquals("2024-01-01T12:00:00Z", codeCovResult.getTimestamp());
    }

    @Test
    public void testCommitid() {
        codeCovResult.setCommitId("abc123");
        assertEquals("abc123", codeCovResult.getCommitId());
    }

    @Test
    public void testState() {
        codeCovResult.setState("success");
        assertEquals("success", codeCovResult.getState());
    }

    @Test
    public void testCoverage() {
        codeCovResult.setCoverage(85.5);
        assertEquals(85.5, codeCovResult.getCoverage());
    }

    @Test
    public void testBranch() {
        codeCovResult.setBranch("main");
        assertEquals("main", codeCovResult.getBranch());
    }

    @Test
    public void testUrl() {
        codeCovResult.setUrl("https://example.com");
        assertEquals("https://example.com", codeCovResult.getUrl());
    }

    @Test
    public void testSerializeNonNullCoverage() throws JsonProcessingException {
        CodeCovResult result = new CodeCovResult();
        result.setId("1");
        result.setCommitId("abc123");
        result.setUrl("https://sample.url");
        result.setState("success");
        result.setCoverage(85.5);
        result.setRepository("sample-repo");
        String json = result.toJson(objectMapper);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> actualMap = mapper.readValue(json, new TypeReference<>() {});
        Map<String, Object> expectedMap = mapper.readValue(
                "{\"id\":\"1\"," +
                        "\"current_date\":null," +
                        "\"repository\":\"sample-repo\"," +
                        "\"component\":null," +
                        "\"release_version\":null," +
                        "\"version\":null," +
                        "\"release_state\":null," +
                        "\"commitid\":\"abc123\"," +
                        "\"state\":\"success\"," +
                        "\"coverage\":85.5," +
                        "\"branch\":null," +
                        "\"url\":\"https://sample.url\"}",
                new TypeReference<>() {
                }
        );
        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void testSerializeZeroCoverage() throws JsonProcessingException {
        CodeCovResult result = new CodeCovResult();
        result.setId("2");
        result.setCommitId("def456");
        result.setUrl("https://sample2.url");
        result.setState("failure");
        result.setCoverage(0.0);
        String json = result.toJson(objectMapper);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> actualMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expectedMap = mapper.readValue(
                "{\"id\":\"2\"," +
                        "\"current_date\":null," +
                        "\"repository\":null," +
                        "\"component\":null," +
                        "\"release_version\":null," +
                        "\"version\":null," +
                        "\"release_state\":null," +
                        "\"commitid\":\"def456\"," +
                        "\"state\":\"failure\"," +
                        "\"coverage\":0.0," +
                        "\"branch\":null," +
                        "\"url\":\"https://sample2.url\"}",
                new TypeReference<>() {
                }
        );
        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void testDeserializeResult() throws JsonProcessingException {
        String json = "{\"id\":\"3\",\"commitid\":\"ghi789\",\"url\":\"https://sample3.url\"," +
                "\"state\":\"partial\",\"coverage\":90.0,\"repository\":\"test-repo\"}";
        CodeCovResult result = objectMapper.readValue(json, CodeCovResult.class);
        assertEquals("3", result.getId());
        assertEquals("ghi789", result.getCommitId());
        assertEquals("https://sample3.url", result.getUrl());
        assertEquals("partial", result.getState());
        assertEquals(90.0, result.getCoverage());
        assertEquals("test-repo", result.getRepository());
    }

    @Test
    public void testDeserializeNullCoverage() throws JsonProcessingException {
        String json = "{\"id\":\"4\",\"commitid\":\"jkl012\",\"url\":\"https://sample4.url\"," +
                "\"state\":\"error\",\"coverage\":null}";
        CodeCovResult result = objectMapper.readValue(json, CodeCovResult.class);
        assertEquals("4", result.getId());
        assertEquals("jkl012", result.getCommitId());
        assertEquals("https://sample4.url", result.getUrl());
        assertEquals("error", result.getState());
        assertNull(result.getCoverage());
    }

    @Test
    public void testGetJsonWithValidData() throws JsonProcessingException {
        CodeCovResult result = new CodeCovResult();
        result.setId("5");
        result.setCommitId("mno345");
        result.setUrl("https://sample5.url");
        result.setState("completed");
        result.setCoverage(75.0);
        String json = result.getJson(result, objectMapper);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> actualMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> expectedMap = mapper.readValue(
                "{\"id\":\"5\"," +
                        "\"current_date\":null," +
                        "\"repository\":null," +
                        "\"component\":null," +
                        "\"release_version\":null," +
                        "\"version\":null," +
                        "\"release_state\":null," +
                        "\"commitid\":\"mno345\"," +
                        "\"state\":\"completed\"," +
                        "\"coverage\":75.0," +
                        "\"branch\":null," +
                        "\"url\":\"https://sample5.url\"}",
                new TypeReference<>() {
                }
        );
        assertEquals(expectedMap, actualMap);
    }
}
