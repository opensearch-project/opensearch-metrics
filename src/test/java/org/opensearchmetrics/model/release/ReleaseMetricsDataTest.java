package org.opensearchmetrics.model.release;

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

public class ReleaseMetricsDataTest {
    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData();
        releaseMetricsData.setId("1");
        releaseMetricsData.setCurrentDate("2024-03-15");
        releaseMetricsData.setRepository("test-repo");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setIssuesOpen(5L);
        releaseMetricsData.setAutocutIssuesOpen(2L);
        releaseMetricsData.setIssuesClosed(3L);
        releaseMetricsData.setPullsOpen(2L);
        releaseMetricsData.setPullsClosed(1L);
        releaseMetricsData.setVersionIncrement(true);
        releaseMetricsData.setReleaseNotes(true);
        releaseMetricsData.setReleaseBranch(false);

        Map<String, Object> expectedData = new HashMap<>();
        expectedData.put("id", "1");
        expectedData.put("current_date", "2024-03-15");
        expectedData.put("repository", "test-repo");
        expectedData.put("release_version", "1.0.0");
        expectedData.put("release_state", "stable");
        expectedData.put("issues_open", 5L);
        expectedData.put("autocut_issues_open", 2L);
        expectedData.put("issues_closed", 3L);
        expectedData.put("pulls_open", 2L);
        expectedData.put("pulls_closed", 1L);
        expectedData.put("version_increment", true);
        expectedData.put("release_notes", true);
        expectedData.put("release_branch", false);

        when(objectMapper.writeValueAsString(expectedData)).thenReturn("expectedJson");

        // Act
        String actualJson = releaseMetricsData.toJson(objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson() throws JsonProcessingException {
        // Arrange
        ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData();
        releaseMetricsData.setId("1");
        releaseMetricsData.setCurrentDate("2024-03-15");
        releaseMetricsData.setRepository("test-repo");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setIssuesOpen(5L);
        releaseMetricsData.setAutocutIssuesOpen(2L);
        releaseMetricsData.setIssuesClosed(3L);
        releaseMetricsData.setPullsOpen(2L);
        releaseMetricsData.setPullsClosed(1L);
        releaseMetricsData.setVersionIncrement(true);
        releaseMetricsData.setReleaseNotes(true);
        releaseMetricsData.setReleaseBranch(false);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("expectedJson");

        // Act
        String actualJson = releaseMetricsData.getJson(releaseMetricsData, objectMapper);

        // Assert
        assertEquals("expectedJson", actualJson);
    }

    @Test
    void getJson_WithJsonProcessingException() throws JsonProcessingException {
        // Arrange
        ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData();
        releaseMetricsData.setId("1");
        releaseMetricsData.setCurrentDate("2024-03-15");
        releaseMetricsData.setRepository("test-repo");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setIssuesOpen(5L);
        releaseMetricsData.setAutocutIssuesOpen(2L);
        releaseMetricsData.setIssuesClosed(3L);
        releaseMetricsData.setPullsOpen(2L);
        releaseMetricsData.setPullsClosed(1L);
        releaseMetricsData.setVersionIncrement(true);
        releaseMetricsData.setReleaseNotes(true);
        releaseMetricsData.setReleaseBranch(false);

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> releaseMetricsData.getJson(releaseMetricsData, objectMapper));
    }
}
