/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.maintainer;

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

public class MaintainerDataTest {

    @Mock
    ObjectMapper objectMapper;

    private MaintainerData maintainerData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        maintainerData = new MaintainerData();
    }

    @Test
    public void testId() {
        maintainerData.setId("123");
        assertEquals("123", maintainerData.getId());
    }

    @Test
    public void testCurrentDate() {
        maintainerData.setCurrentDate("2024-03-20");
        assertEquals("2024-03-20", maintainerData.getCurrentDate());
    }

    @Test
    public void testRepository() {
        maintainerData.setRepository("exampleRepo");
        assertEquals("exampleRepo", maintainerData.getRepository());
    }

    @Test
    public void testName() {
        maintainerData.setName("Alejandro Rosalez");
        assertEquals("Alejandro Rosalez", maintainerData.getName());
    }

    @Test
    public void testGithubLogin() {
        maintainerData.setGithubLogin("alejandro_rosalez");
        assertEquals("alejandro_rosalez", maintainerData.getGithubLogin());
    }

    @Test
    public void testAffiliation() {
        maintainerData.setAffiliation("Amazon");
        assertEquals("Amazon", maintainerData.getAffiliation());
    }

    @Test
    public void testEventType() {
        maintainerData.setEventType("IssuesEvent");
        assertEquals("IssuesEvent", maintainerData.getEventType());
    }

    @Test
    public void testEventAction() {
        maintainerData.setEventAction("edited");
        assertEquals("edited", maintainerData.getEventAction());
    }

    @Test
    public void testTimeLastEngaged() {
        maintainerData.setTimeLastEngaged("2024-09-23T20:14:08.346Z");
        assertEquals("2024-09-23T20:14:08.346Z", maintainerData.getTimeLastEngaged());
    }

    @Test
    public void testInactive() {
        maintainerData.setInactive(true);
        assertEquals(true, maintainerData.isInactive());
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        MaintainerData maintainerData = new MaintainerData();
        maintainerData.setId("1");
        maintainerData.setCurrentDate("2024-03-15");
        maintainerData.setRepository("test-repo");
        maintainerData.setName("Alejandro Rosalez");
        maintainerData.setGithubLogin("alejandro_rosalez");
        maintainerData.setAffiliation("Amazon");
        maintainerData.setEventType("IssuesEvent");
        maintainerData.setEventAction("edited");
        maintainerData.setTimeLastEngaged("2024-09-23T20:14:08.346Z");
        maintainerData.setInactive(true);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", "1");
        expectedData.put("current_date", "2024-03-15");
        expectedData.put("repository", "test-repo");
        expectedData.put("name", "Alejandro Rosalez");
        expectedData.put("github_login", "alejandro_rosalez");
        expectedData.put("affiliation", "Amazon");
        expectedData.put("event_type", "IssuesEvent");
        expectedData.put("event_action", "edited");
        expectedData.put("time_last_engaged", "2024-09-23T20:14:08.346Z");
        expectedData.put("inactive", true);

        when(objectMapper.writeValueAsString(expectedData)).thenReturn("expectedJson");

        // Act
        String actualJson = maintainerData.toJson(objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson() throws JsonProcessingException {
        // Arrange
        MaintainerData maintainerData = new MaintainerData();
        maintainerData.setId("1");
        maintainerData.setCurrentDate("2024-03-15");
        maintainerData.setRepository("test-repo");
        maintainerData.setName("Alejandro Rosalez");
        maintainerData.setGithubLogin("alejandro_rosalez");
        maintainerData.setAffiliation("Amazon");
        maintainerData.setEventType("IssuesEvent");
        maintainerData.setEventAction("edited");
        maintainerData.setTimeLastEngaged("2024-09-23T20:14:08.346Z");
        maintainerData.setInactive(true);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("expectedJson");

        // Act
        String actualJson = maintainerData.getJson(maintainerData, objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson_WithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        MaintainerData maintainerData = new MaintainerData();
        maintainerData.setId("1");
        maintainerData.setCurrentDate("2024-03-15");
        maintainerData.setRepository("test-repo");
        maintainerData.setName("Alejandro Rosalez");
        maintainerData.setGithubLogin("alejandro_rosalez");
        maintainerData.setAffiliation("Amazon");
        maintainerData.setEventType("IssuesEvent");
        maintainerData.setEventAction("edited");
        maintainerData.setTimeLastEngaged("2024-09-23T20:14:08.346Z");
        maintainerData.setInactive(true);

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> maintainerData.getJson(maintainerData, objectMapper));
    }
}
