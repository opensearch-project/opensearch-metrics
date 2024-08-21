package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    @Test
    public void testGetReleaseRepos() {
        MockitoAnnotations.openMocks(this);
        List<String> repos = Collections.singletonList("testRepo");
        when(releaseRepoFetcher.getReleaseRepos(anyString())).thenReturn(repos);
        List<String> result = releaseRepoFetcher.getReleaseRepos("1.0.0");
        assertSame(repos, result);
    }

    @Test
    public void testGetReleaseNotes() {
        MockitoAnnotations.openMocks(this);

        boolean expectedNotes = true;
        when(releaseNotesChecker.releaseNotes(anyString(), anyString(), anyString()))
                .thenReturn(expectedNotes);
        boolean result = releaseNotesChecker.releaseNotes("1.0.0", "testRepo", "1.0");
        assertEquals(expectedNotes, result);
    }

    @Test
    public void testGetReleaseLabelIssues() {
        MockitoAnnotations.openMocks(this);

        long expectedIssuesCount = 10;
        when(releaseLabelIssuesFetcher.releaseLabelIssues(anyString(), anyString(), anyString(), anyBoolean(), any()))
                .thenReturn(expectedIssuesCount);

        long result = releaseLabelIssuesFetcher.releaseLabelIssues("1.0.0", "testRepo", "open", true, openSearchUtil);

        assertEquals(expectedIssuesCount, result);
    }

    @Test
    public void testGetReleaseLabelPulls() {
        MockitoAnnotations.openMocks(this);
        long expectedPullsCount = 5;
        when(releaseLabelPullsFetcher.releaseLabelPulls(anyString(), anyString(), anyString(), any()))
                .thenReturn(expectedPullsCount);
        long result = releaseLabelPullsFetcher.releaseLabelPulls("1.0.0", "testRepo", "open", openSearchUtil);
        assertEquals(expectedPullsCount, result);
    }

    @Test
    public void testGetReleaseVersionIncrement() {
        MockitoAnnotations.openMocks(this);
        boolean expectedIncrement = true;
        when(releaseVersionIncrementChecker.releaseVersionIncrement(anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(expectedIncrement);
        boolean result = releaseVersionIncrementChecker.releaseVersionIncrement("1.0.0", "testRepo", "main", objectMapper, openSearchUtil);
        assertEquals(expectedIncrement, result);
    }

    @Test
    public void testGetReleaseBranch() {
        MockitoAnnotations.openMocks(this);
        boolean expectedBranch = true;
        when(releaseBranchChecker.releaseBranch(anyString(), anyString()))
                .thenReturn(expectedBranch);
        boolean result = releaseBranchChecker.releaseBranch("1.0.0", "testRepo");
        assertEquals(expectedBranch, result);
    }

    @Test
    public void testGetReleaseOwner() {
        MockitoAnnotations.openMocks(this);
        String[] releaseOwners = new String[]{"sample_user_1"};
        when(releaseIssueChecker.releaseOwners(anyString(), anyString(), any()))
                .thenReturn(releaseOwners);
        String[] result = releaseIssueChecker.releaseOwners("1.0.0", "testRepo", openSearchUtil);
        assertEquals(releaseOwners, result);
    }

    @Test
    public void testGetReleaseIssue() {
        MockitoAnnotations.openMocks(this);
        String releaseIssue = "https://sample-release-issue/100";
        when(releaseIssueChecker.releaseIssue(anyString(), anyString(), any()))
                .thenReturn(releaseIssue);
        String result = releaseIssueChecker.releaseIssue("1.0.0", "testRepo", openSearchUtil);
        assertEquals(releaseIssue, result);
    }
}
