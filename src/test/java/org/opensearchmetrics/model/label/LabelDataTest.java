package org.opensearchmetrics.model.label;

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

public class LabelDataTest {

    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        LabelData labelData = new LabelData();
        labelData.setId("1");
        labelData.setCurrentDate("2024-03-15");
        labelData.setRepository("test-repo");
        labelData.setLabelName("bug");
        labelData.setLabelIssueCount(10L);
        labelData.setLabelPullCount(5L);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", "1");
        expectedData.put("current_date", "2024-03-15");
        expectedData.put("repository", "test-repo");
        expectedData.put("label_name", "bug");
        expectedData.put("label_issue_count", 10L);
        expectedData.put("label_pull_count", 5L);

        when(objectMapper.writeValueAsString(expectedData)).thenReturn("expectedJson");

        // Act
        String actualJson = labelData.toJson(objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson() throws JsonProcessingException {
        // Arrange
        LabelData labelData = new LabelData();
        labelData.setId("1");
        labelData.setCurrentDate("2024-03-15");
        labelData.setRepository("test-repo");
        labelData.setLabelName("bug");
        labelData.setLabelIssueCount(10L);
        labelData.setLabelPullCount(5L);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("expectedJson");

        // Act
        String actualJson = labelData.getJson(labelData, objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson_WithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        LabelData labelData = new LabelData();
        labelData.setId("1");
        labelData.setCurrentDate("2024-03-15");
        labelData.setRepository("test-repo");
        labelData.setLabelName("bug");
        labelData.setLabelIssueCount(10L);
        labelData.setLabelPullCount(5L);

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> labelData.getJson(labelData, objectMapper));
    }
}
