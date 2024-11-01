/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

public class EventDataTest {
    @Mock
    ObjectMapper objectMapper;

    private EventData eventData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventData = new EventData();
    }

    @Test
    public void testId() {
        eventData.setId("123");
        assertEquals("123", eventData.getId());
    }

    @Test
    public void testOrganization() {
        eventData.setOrganization("exampleOrg");
        assertEquals("exampleOrg", eventData.getOrganization());
    }

    @Test
    public void testRepository() {
        eventData.setRepository("exampleRepo");
        assertEquals("exampleRepo", eventData.getRepository());
    }

    @Test
    public void testType() {
        eventData.setType("issues");
        assertEquals("issues", eventData.getType());
    }

    @Test
    public void testAction() {
        eventData.setAction("opened");
        assertEquals("opened", eventData.getAction());
    }

    @Test
    public void testSender() {
        eventData.setSender("Alejandro Rosalez");
        assertEquals("Alejandro Rosalez", eventData.getSender());
    }

    @Test
    public void testCreatedAt() {
        eventData.setCreatedAt("2024-09-23T20:14:08.346Z");
        assertEquals("2024-09-23T20:14:08.346Z", eventData.getCreatedAt());
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        EventData eventData = new EventData();
        eventData.setId("1");
        eventData.setOrganization("test-org");
        eventData.setRepository("test-repo");
        eventData.setType("pull_request");
        eventData.setAction("closed");
        eventData.setSender("Alejandro Rosalez");
        eventData.setCreatedAt("2024-09-23T20:14:08.346Z");

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", "1");
        expectedData.put("organization", "test-org");
        expectedData.put("repository", "test-repo");
        expectedData.put("type", "pull_request");
        expectedData.put("action", "closed");
        expectedData.put("sender", "Alejandro Rosalez");
        expectedData.put("created_at", "2024-09-23T20:14:08.346Z");

        when(objectMapper.writeValueAsString(expectedData)).thenReturn("expectedJson");

        // Act
        String actualJson = eventData.toJson(objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson() throws JsonProcessingException {
        // Arrange
        EventData eventData = new EventData();
        eventData.setId("1");
        eventData.setOrganization("test-org");
        eventData.setRepository("test-repo");
        eventData.setType("pull_request");
        eventData.setAction("closed");
        eventData.setSender("Alejandro Rosalez");
        eventData.setCreatedAt("2024-09-23T20:14:08.346Z");

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("expectedJson");

        // Act
        String actualJson = eventData.getJson(eventData, objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson_WithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        EventData eventData = new EventData();
        eventData.setId("1");
        eventData.setOrganization("test-org");
        eventData.setRepository("test-repo");
        eventData.setType("pull_request");
        eventData.setAction("closed");
        eventData.setSender("Alejandro Rosalez");
        eventData.setCreatedAt("2024-09-23T20:14:08.346Z");

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> eventData.getJson(eventData, objectMapper));
    }
}
