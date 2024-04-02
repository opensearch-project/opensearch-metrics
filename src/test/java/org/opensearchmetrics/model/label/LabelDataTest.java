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

    private LabelData labelData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        labelData = new LabelData();
    }

    @Test
    public void testId() {
        labelData.setId("123");
        assertEquals("123", labelData.getId());
    }

    @Test
    public void testCurrentDate() {
        labelData.setCurrentDate("2024-03-20");
        assertEquals("2024-03-20", labelData.getCurrentDate());
    }

    @Test
    public void testRepository() {
        labelData.setRepository("exampleRepo");
        assertEquals("exampleRepo", labelData.getRepository());
    }

    @Test
    public void testLabelName() {
        labelData.setLabelName("bug");
        assertEquals("bug", labelData.getLabelName());
    }

    @Test
    public void testLabelIssueCount() {
        labelData.setLabelIssueCount(10L);
        assertEquals(10L, labelData.getLabelIssueCount());
    }

    @Test
    public void testLabelPullCount() {
        labelData.setLabelPullCount(5L);
        assertEquals(5L, labelData.getLabelPullCount());
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
