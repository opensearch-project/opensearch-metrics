/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearchmetrics.model.codecov.CodeCovResponse;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ReleaseMetricsTest {

    @Mock
    private OpenSearchUtil openSearchUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReleaseRepoFetcher releaseRepoFetcher;

    @Mock
    private ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher;

    @Mock
    private ReleaseLabelPullsFetcher releaseLabelPullsFetcher;

    @Mock
    private ReleaseVersionIncrementChecker releaseVersionIncrementChecker;

    @Mock
    private ReleaseBranchChecker releaseBranchChecker;

    @Mock
    private ReleaseNotesChecker releaseNotesChecker;

    @Mock
    private ReleaseIssueChecker releaseIssueChecker;

    @Mock
    private CodeCoverage codeCoverage;

    @InjectMocks
    private ReleaseMetrics releaseMetrics;

    private CodeCovResponse codeCovResponse;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        codeCovResponse = new CodeCovResponse();
        codeCovResponse.setCommitId("abc123");
        codeCovResponse.setUrl("https://sample-release-issue/100");
        codeCovResponse.setState("success");
        codeCovResponse.setCoverage(85.5);
    }

    @Test
    public void testGetReleaseRepos() {
        Map<String, String> repos = Collections.singletonMap("testRepo", "testComponent");
        when(releaseRepoFetcher.getReleaseRepos(anyString())).thenReturn(repos);

        Map<String, String> result = releaseMetrics.getReleaseRepos("1.0.0");
        assertEquals(repos, result);
    }

    @Test
    public void testGetReleaseNotes() {
        boolean expectedNotes = true;
        when(releaseNotesChecker.releaseNotes(anyString(), anyString(), anyString()))
                .thenReturn(expectedNotes);

        boolean result = releaseMetrics.getReleaseNotes("1.0.0", "testRepo", "1.0");
        assertEquals(expectedNotes, result);
    }

    @Test
    public void testGetReleaseLabelIssues() {
        long expectedIssuesCount = 10;
        when(releaseLabelIssuesFetcher.releaseLabelIssues(anyString(), anyString(), anyString(), anyBoolean(), any()))
                .thenReturn(expectedIssuesCount);

        long result = releaseMetrics.getReleaseLabelIssues("1.0.0", "testRepo", "open", true);
        assertEquals(expectedIssuesCount, result);
    }

    @Test
    public void testGetReleaseLabelPulls() {
        long expectedPullsCount = 5;
        when(releaseLabelPullsFetcher.releaseLabelPulls(anyString(), anyString(), anyString(), any()))
                .thenReturn(expectedPullsCount);

        long result = releaseMetrics.getReleaseLabelPulls("1.0.0", "testRepo", "open");
        assertEquals(expectedPullsCount, result);
    }

    @Test
    public void testGetReleaseVersionIncrement() {
        boolean expectedIncrement = true;
        when(releaseVersionIncrementChecker.releaseVersionIncrement(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(expectedIncrement);

        boolean result = releaseMetrics.getReleaseVersionIncrement("1.0.0", "testRepo", "main");
        assertEquals(expectedIncrement, result);
    }

    @Test
    public void testGetReleaseBranch() {
        boolean expectedBranch = true;
        when(releaseBranchChecker.releaseBranch(anyString(), anyString()))
                .thenReturn(expectedBranch);

        boolean result = releaseMetrics.getReleaseBranch("1.0.0", "testRepo");
        assertEquals(expectedBranch, result);
    }

    @Test
    public void testGetReleaseOwners() {
        String[] expectedOwners = new String[]{"sample_user_1"};
        when(releaseIssueChecker.releaseOwners(anyString(), anyString(), any()))
                .thenReturn(expectedOwners);

        String[] result = releaseMetrics.getReleaseOwners("1.0.0", "testRepo");
        assertArrayEquals(expectedOwners, result);
    }

    @Test
    public void testGetReleaseIssue() {
        String expectedIssue = "https://sample-release-issue/100";
        when(releaseIssueChecker.releaseIssue(anyString(), anyString(), any()))
                .thenReturn(expectedIssue);

        String result = releaseMetrics.getReleaseIssue("1.0.0", "testRepo");
        assertEquals(expectedIssue, result);
    }

    @Test
    public void testGetCodeCoverage() {
        when(codeCoverage.coverage(anyString(), anyString())).thenReturn(codeCovResponse);

        CodeCovResponse result = releaseMetrics.getCodeCoverage("2.18", "testRepo");
        assertEquals("abc123", result.getCommitId());
        assertEquals("https://sample-release-issue/100", result.getUrl());
        assertEquals("success", result.getState());
        assertEquals(85.5, result.getCoverage());
    }
}
