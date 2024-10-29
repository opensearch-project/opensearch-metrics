/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.general;

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

public class MetricsDataTest {

    @Mock
    ObjectMapper objectMapper;

    private MetricsData metricsData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        metricsData = new MetricsData();
    }

    @Test
    public void testId() {
        metricsData.setId("123");
        assertEquals("123", metricsData.getId());
    }

    @Test
    public void testCurrentDate() {
        metricsData.setCurrentDate("2024-03-20");
        assertEquals("2024-03-20", metricsData.getCurrentDate());
    }

    @Test
    public void testRepository() {
        metricsData.setRepository("exampleRepo");
        assertEquals("exampleRepo", metricsData.getRepository());
    }

    @Test
    public void testMetricName() {
        metricsData.setMetricName("coverage");
        assertEquals("coverage", metricsData.getMetricName());
    }

    @Test
    public void testMetricCount() {
        metricsData.setMetricCount(100L);
        assertEquals(100L, metricsData.getMetricCount());
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        MetricsData metricsData = new MetricsData();
        metricsData.setId("1");
        metricsData.setCurrentDate("2024-03-15");
        metricsData.setRepository("test-repo");
        metricsData.setMetricName("bugs");
        metricsData.setMetricCount(10L);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", "1");
        expectedData.put("current_date", "2024-03-15");
        expectedData.put("repository", "test-repo");
        expectedData.put("metric_name", "bugs");
        expectedData.put("metric_count", 10L);

        when(objectMapper.writeValueAsString(expectedData)).thenReturn("expectedJson");

        // Act
        String actualJson = metricsData.toJson(objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson() throws JsonProcessingException {
        // Arrange
        MetricsData metricsData = new MetricsData();
        metricsData.setId("1");
        metricsData.setCurrentDate("2024-03-15");
        metricsData.setRepository("test-repo");
        metricsData.setMetricName("bugs");
        metricsData.setMetricCount(10L);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("expectedJson");

        // Act
        String actualJson = metricsData.getJson(metricsData, objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson_WithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        MetricsData metricsData = new MetricsData();
        metricsData.setId("1");
        metricsData.setCurrentDate("2024-03-15");
        metricsData.setRepository("test-repo");
        metricsData.setMetricName("bugs");
        metricsData.setMetricCount(10L);

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> metricsData.getJson(metricsData, objectMapper));
    }
}
