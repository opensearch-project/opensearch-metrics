/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
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

    private ReleaseMetricsData releaseMetricsData;

    private static String[] SAMPLE_USERS = new String[]{"sample_user"};

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        releaseMetricsData = new ReleaseMetricsData();
    }

    @Test
    public void testId() {
        releaseMetricsData.setId("123");
        assertEquals("123", releaseMetricsData.getId());
    }

    @Test
    public void testCurrentDate() {
        releaseMetricsData.setCurrentDate("2024-03-20");
        assertEquals("2024-03-20", releaseMetricsData.getCurrentDate());
    }

    @Test
    public void testRepository() {
        releaseMetricsData.setRepository("exampleRepo");
        assertEquals("exampleRepo", releaseMetricsData.getRepository());
    }

    @Test
    public void testCOmponent() {
        releaseMetricsData.setComponent("exampleComponent");
        assertEquals("exampleComponent", releaseMetricsData.getComponent());
    }

    @Test
    public void testReleaseVersion() {
        releaseMetricsData.setReleaseVersion("1.0");
        assertEquals("1.0", releaseMetricsData.getReleaseVersion());
    }

    @Test
    public void testVersion() {
        releaseMetricsData.setVersion("1.0");
        assertEquals("1.0", releaseMetricsData.getVersion());
    }

    @Test
    public void testReleaseState() {
        releaseMetricsData.setReleaseState("planned");
        assertEquals("planned", releaseMetricsData.getReleaseState());
    }

    @Test
    public void testIssuesOpen() {
        releaseMetricsData.setIssuesOpen(10L);
        assertEquals(10L, releaseMetricsData.getIssuesOpen());
    }

    @Test
    public void testAutocutIssuesOpen() {
        releaseMetricsData.setAutocutIssuesOpen(5L);
        assertEquals(5L, releaseMetricsData.getAutocutIssuesOpen());
    }

    @Test
    public void testIssuesClosed() {
        releaseMetricsData.setIssuesClosed(20L);
        assertEquals(20L, releaseMetricsData.getIssuesClosed());
    }

    @Test
    public void testPullsOpen() {
        releaseMetricsData.setPullsOpen(3L);
        assertEquals(3L, releaseMetricsData.getPullsOpen());
    }

    @Test
    public void testPullsClosed() {
        releaseMetricsData.setPullsClosed(7L);
        assertEquals(7L, releaseMetricsData.getPullsClosed());
    }

    @Test
    public void testVersionIncrement() {
        releaseMetricsData.setVersionIncrement(true);
        assertEquals(true, releaseMetricsData.isVersionIncrement());
    }

    @Test
    public void testReleaseNotes() {
        releaseMetricsData.setReleaseNotes(true);
        assertEquals(true, releaseMetricsData.isReleaseNotes());
    }

    @Test
    public void testReleaseBranch() {
        releaseMetricsData.setReleaseBranch(true);
        assertEquals(true, releaseMetricsData.isReleaseBranch());
    }

    @Test
    public void testReleaseOwners() {
        releaseMetricsData.setReleaseOwners(SAMPLE_USERS);
        assertEquals(SAMPLE_USERS, releaseMetricsData.getReleaseOwners());
    }

    @Test
    public void testReleaseOwnerExists() {
        releaseMetricsData.setReleaseOwnerExists(false);
        assertEquals(false, releaseMetricsData.isReleaseOwnerExists());
    }

    @Test
    public void testReleaseIssue() {
        releaseMetricsData.setReleaseIssue("https://sample-release-issue/100");
        assertEquals("https://sample-release-issue/100", releaseMetricsData.getReleaseIssue());
    }

    @Test
    public void testReleaseIssueExists() {
        releaseMetricsData.setReleaseIssueExists(false);
        assertEquals(false, releaseMetricsData.isReleaseIssueExists());
    }

    @Test
    void toJson() throws JsonProcessingException {
        // Arrange
        ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData();
        releaseMetricsData.setId("1");
        releaseMetricsData.setCurrentDate("2024-03-15");
        releaseMetricsData.setRepository("test-repo");
        releaseMetricsData.setComponent("test-component");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setVersion("1.0.0");
        releaseMetricsData.setReleaseIssue("https://sample-release-issue/100");
        releaseMetricsData.setReleaseIssueExists(true);
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setReleaseOwners(SAMPLE_USERS);
        releaseMetricsData.setReleaseOwnerExists(true);
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
        expectedData.put("component", "test-component");
        expectedData.put("release_version", "1.0.0");
        expectedData.put("version", "1.0.0");
        expectedData.put("release_state", "stable");
        expectedData.put("issues_open", 5L);
        expectedData.put("autocut_issues_open", 2L);
        expectedData.put("issues_closed", 3L);
        expectedData.put("pulls_open", 2L);
        expectedData.put("pulls_closed", 1L);
        expectedData.put("version_increment", true);
        expectedData.put("release_notes", true);
        expectedData.put("release_branch", false);
        expectedData.put("release_owners", SAMPLE_USERS);
        expectedData.put("release_owner_exists", true);
        expectedData.put("release_issue","https://sample-release-issue/100");
        expectedData.put("release_issue_exists", true);

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
        releaseMetricsData.setComponent("test-component");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setVersion("1.0.0");
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setIssuesOpen(5L);
        releaseMetricsData.setAutocutIssuesOpen(2L);
        releaseMetricsData.setIssuesClosed(3L);
        releaseMetricsData.setPullsOpen(2L);
        releaseMetricsData.setPullsClosed(1L);
        releaseMetricsData.setVersionIncrement(true);
        releaseMetricsData.setReleaseNotes(true);
        releaseMetricsData.setReleaseBranch(false);
        releaseMetricsData.setReleaseOwners(SAMPLE_USERS);
        releaseMetricsData.setReleaseOwnerExists(true);
        releaseMetricsData.setReleaseIssue("https://sample-release-issue/100");
        releaseMetricsData.setReleaseIssueExists(true);

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
        releaseMetricsData.setComponent("test-component");
        releaseMetricsData.setReleaseVersion("1.0.0");
        releaseMetricsData.setVersion("1.0.0");
        releaseMetricsData.setReleaseState("stable");
        releaseMetricsData.setIssuesOpen(5L);
        releaseMetricsData.setAutocutIssuesOpen(2L);
        releaseMetricsData.setIssuesClosed(3L);
        releaseMetricsData.setPullsOpen(2L);
        releaseMetricsData.setPullsClosed(1L);
        releaseMetricsData.setVersionIncrement(true);
        releaseMetricsData.setReleaseNotes(true);
        releaseMetricsData.setReleaseBranch(false);
        releaseMetricsData.setReleaseOwners(SAMPLE_USERS);
        releaseMetricsData.setReleaseOwnerExists(true);
        releaseMetricsData.setReleaseIssue("https://sample-release-issue/100");
        releaseMetricsData.setReleaseIssueExists(true);

        when(objectMapper.writeValueAsString(anyMap())).thenThrow(JsonProcessingException.class);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> releaseMetricsData.getJson(releaseMetricsData, objectMapper));
    }
}
