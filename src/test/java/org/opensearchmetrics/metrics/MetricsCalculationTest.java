/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchmetrics.metrics.general.*;
import org.opensearchmetrics.metrics.label.LabelMetrics;
import org.opensearchmetrics.metrics.maintainer.MaintainerMetrics;
import org.opensearchmetrics.metrics.release.ReleaseInputs;
import org.opensearchmetrics.metrics.release.ReleaseMetrics;
import org.opensearchmetrics.model.codecov.CodeCovResponse;
import org.opensearchmetrics.model.label.LabelData;
import org.opensearchmetrics.model.general.MetricsData;
import org.opensearchmetrics.model.maintainer.LatestEventData;
import org.opensearchmetrics.model.maintainer.MaintainerData;
import org.opensearchmetrics.model.release.ReleaseMetricsData;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetricsCalculationTest {
    @Mock
    private OpenSearchUtil openSearchUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UntriagedIssues untriagedIssues;
    @Mock
    private UncommentedPullRequests uncommentedPullRequests;
    @Mock
    private UnlabelledPullRequests unlabelledPullRequests;
    @Mock
    private UnlabelledIssues unlabelledIssues;
    @Mock
    private MergedPullRequests mergedPullRequests;
    @Mock
    private OpenPullRequests openPullRequests;
    @Mock
    private OpenIssues openIssues;
    @Mock
    private ClosedIssues closedIssues;
    @Mock
    private CreatedIssues createdIssues;
    @Mock
    private IssueComments issueComments;
    @Mock
    private PullComments pullComments;
    @Mock
    private IssuePositiveReactions issuePositiveReactions;
    @Mock
    private IssueNegativeReactions issueNegativeReactions;
    @Mock
    private LabelMetrics labelMetrics;
    @Mock
    private ReleaseMetrics releaseMetrics;
    @Mock
    private MaintainerMetrics maintainerMetrics;


    @InjectMocks
    private MetricsCalculation metricsCalculation;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        metricsCalculation = new MetricsCalculation(openSearchUtil, objectMapper,
                untriagedIssues, uncommentedPullRequests, unlabelledPullRequests, unlabelledIssues,
                mergedPullRequests, openPullRequests, openIssues, closedIssues, createdIssues,
                issueComments, pullComments, issuePositiveReactions, issueNegativeReactions,
                labelMetrics, releaseMetrics, maintainerMetrics);
    }

    @Test
    void testGenerateGeneralMetrics() throws IOException {
        List<String> repositories = Arrays.asList("repo1", "repo2");
        MetricsData metricsData = new MetricsData();
        metricsData.setId("id");
        metricsData.setRepository("repo1");
        metricsData.setCurrentDate(LocalDateTime.now(ZoneId.of("UTC")).toString());
        metricsData.setMetricName("metric");
        metricsData.setMetricCount(10L);
        when(untriagedIssues.getBoolQueryBuilder(any())).thenReturn(new BoolQueryBuilder());
        when(untriagedIssues.createSearchRequest(any(), any())).thenReturn(new SearchRequest());
        when(untriagedIssues.performSearch(any(), any())).thenReturn(10L);
        when(objectMapper.writeValueAsString(any())).thenReturn("json");
        metricsCalculation.generateGeneralMetrics(repositories);
        verify(openSearchUtil).createIndexIfNotExists("opensearch_general_metrics");
        verify(openSearchUtil).bulkIndex(eq("opensearch_general_metrics"), any(Map.class));
    }

    @Test
    void testGenerateLabelMetrics() throws IOException {
        List<String> repositories = Arrays.asList("repo1", "repo2");
        Map<String, List<Long>> labelInfo = new HashMap<>();
        labelInfo.put("label1", Arrays.asList(5L, 10L));
        LabelData labelData = new LabelData();
        labelData.setId("id");
        labelData.setRepository("repo1");
        labelData.setCurrentDate(LocalDateTime.now(ZoneId.of("UTC")).toString());
        labelData.setLabelName("label1");
        labelData.setLabelPullCount(10L);
        labelData.setLabelIssueCount(5L);
        when(labelMetrics.getLabelInfo(any(), any())).thenReturn(labelInfo);
        when(objectMapper.writeValueAsString(any())).thenReturn("json");
        metricsCalculation.generateLabelMetrics(repositories);
        verify(openSearchUtil).createIndexIfNotExists("opensearch_label_metrics");
        verify(openSearchUtil).bulkIndex(eq("opensearch_label_metrics"), any(Map.class));
    }

    @Test
    void testGenerateReleaseMetrics() {
        Map<String, String> releaseRepos = new HashMap<>();
        releaseRepos.put("repo1", "component1");
        releaseRepos.put("repo2", "component2");
        when(releaseMetrics.getReleaseRepos("2.13.0")).thenReturn(releaseRepos);
        when(releaseMetrics.getReleaseLabelIssues(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "open", false)).thenReturn(10L);
        when(releaseMetrics.getReleaseLabelIssues(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "open", true)).thenReturn(5L);
        when(releaseMetrics.getReleaseLabelIssues(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "closed", false)).thenReturn(20L);
        when(releaseMetrics.getReleaseLabelPulls(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "open")).thenReturn(3L);
        when(releaseMetrics.getReleaseLabelPulls(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "closed")).thenReturn(8L);
        when(releaseMetrics.getReleaseVersionIncrement(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1", "main")).thenReturn(true);
        when(releaseMetrics.getReleaseNotes(ReleaseInputs.VERSION_2_13_0.getFullVersion(), "repo1", "main")).thenReturn(true);
        when(releaseMetrics.getReleaseBranch(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1")).thenReturn(true);
        when(releaseMetrics.getReleaseOwners(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1")).thenReturn(new String[]{"owner1", "owner2"});
        when(releaseMetrics.getReleaseIssue(ReleaseInputs.VERSION_2_13_0.getVersion(), "repo1")).thenReturn("release-123");
        metricsCalculation.generateReleaseMetrics();
        verify(openSearchUtil).createIndexIfNotExists("opensearch_release_metrics");
        verify(openSearchUtil).bulkIndex(eq("opensearch_release_metrics"), ArgumentMatchers.anyMap());
        verify(openSearchUtil, times(1)).createIndexIfNotExists("opensearch_release_metrics");
    }

    @Test
    void testGenerateCodeCovMetrics() {
        try (MockedStatic<ReleaseInputs> mockedReleaseInputs = Mockito.mockStatic(ReleaseInputs.class)) {
            ReleaseInputs releaseInput = mock(ReleaseInputs.class);
            when(releaseInput.getFullVersion()).thenReturn("2.18.0");
            when(releaseInput.getBranch()).thenReturn("main");
            when(releaseInput.getTrack()).thenReturn(true);
            when(releaseInput.getState()).thenReturn("active");
            ReleaseInputs[] releaseInputsArray = {releaseInput};
            mockedReleaseInputs.when(ReleaseInputs::getAllReleaseInputs).thenReturn(releaseInputsArray);
            Map<String, String> releaseRepos = new HashMap<>();
            releaseRepos.put("repo1", "component1");
            when(releaseMetrics.getReleaseRepos("2.18.0")).thenReturn(releaseRepos);
            CodeCovResponse codeCovResponse = new CodeCovResponse();
            codeCovResponse.setCommitId("abc123");
            codeCovResponse.setUrl("https://sample-url.com");
            codeCovResponse.setState("success");
            codeCovResponse.setCoverage(85.5);
            when(releaseMetrics.getCodeCoverage("main", "repo1")).thenReturn(codeCovResponse);
            try {
                when(objectMapper.writeValueAsString(any())).thenReturn("{}");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            metricsCalculation.generateCodeCovMetrics();
            verify(openSearchUtil).createIndexIfNotExists(matches("opensearch-codecov-metrics-\\d{2}-\\d{4}"));
            verify(openSearchUtil).bulkIndex(matches("opensearch-codecov-metrics-\\d{2}-\\d{4}"), argThat(map -> !map.isEmpty()));
            verify(releaseMetrics).getCodeCoverage("main", "repo1");
            verify(releaseMetrics).getReleaseRepos("2.18.0");
        }
    }

    @Test
    void testGenerateMaintainerMetrics() throws IOException{
        List<String> repositories = Arrays.asList("repo1", "repo2");
        List<String> eventList = Arrays.asList("event1", "event2");
        List<MaintainerData> maintainersList = new ArrayList<>();
        MaintainerData maintainerData = new MaintainerData();
        maintainerData.setRepository("repo1");
        maintainerData.setName("maintainer1");
        maintainerData.setGithubLogin("githubId");
        maintainerData.setAffiliation("affiliation1");
        maintainersList.add(maintainerData);
        LatestEventData latestEventData = new LatestEventData();
        latestEventData.setEventType("eventType");
        latestEventData.setEventAction("eventAction");
        latestEventData.setTimeLastEngaged(Instant.now().minus(7, ChronoUnit.DAYS));
        double[] slopeAndIntercept = {-1.0, 368.0};
        double upperBound = 365;
        double lowerBound = 90;
        when(maintainerMetrics.mostAndLeastRepoEventCounts(any())).thenReturn(new long[]{100L, 10L});
        when(maintainerMetrics.getSlopeAndIntercept(10, upperBound, 100, lowerBound)).thenReturn(slopeAndIntercept);
        when(maintainerMetrics.getEventTypes(any())).thenReturn(eventList);
        when(maintainerMetrics.repoEventCount(any(), any())).thenReturn(50L);
        when(maintainerMetrics.repoMaintainers(any())).thenReturn(maintainersList);
        when(maintainerMetrics.queryLatestEvent(any(), any(), any(), any())).thenReturn(Optional.of(latestEventData));
        when(maintainerMetrics.calculateInactivity(50L, slopeAndIntercept, lowerBound, latestEventData)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("json");
        metricsCalculation.generateMaintainerMetrics(repositories);
        verify(openSearchUtil).createIndexIfNotExists(matches("maintainer-inactivity-\\d{2}-\\d{4}"));
        verify(openSearchUtil).bulkIndex(matches("maintainer-inactivity-\\d{2}-\\d{4}"), argThat(map -> !map.isEmpty()));
    }
}
