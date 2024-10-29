/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.label;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LabelMetricsTest {


    @Test
    public void testGetLabelInfo() throws IOException {
        // Mock OpenSearchUtil
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);

        // Mock search responses
        SearchResponse issuesResponse = Mockito.mock(SearchResponse.class);
        when(issuesResponse.status()).thenReturn(RestStatus.OK);

        // Mock issues aggregation
        ParsedStringTerms issuesTerms = Mockito.mock(ParsedStringTerms.class);
        when(issuesTerms.getBuckets()).thenReturn(new ArrayList<>());
        Aggregations aggregations = Mockito.mock(Aggregations.class);
        when(aggregations.get("label_issues")).thenReturn(issuesTerms);
        when(issuesResponse.getAggregations()).thenReturn(aggregations);

        // Mock pull issues aggregation
        ParsedStringTerms pullIssuesTerms = Mockito.mock(ParsedStringTerms.class);
        when(pullIssuesTerms.getBuckets()).thenReturn(new ArrayList<>());
        when(aggregations.get("pull_issues")).thenReturn(pullIssuesTerms);

        // Mock search request
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(issuesResponse);

        // Instantiate LabelMetrics
        LabelMetrics labelMetrics = new LabelMetrics();

        // Call method under test
        Map<String, List<Long>> labelInfo = labelMetrics.getLabelInfo("repo", openSearchUtil);

        // Assertions
        assertEquals(new HashMap<>(), labelInfo); // Modify expected result according to your logic
    }

    @Test
    public void testGetLabelIssues() throws IOException {
        // Mock OpenSearchUtil
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);

        // Mock search responses
        SearchResponse issuesResponse = Mockito.mock(SearchResponse.class);
        when(issuesResponse.status()).thenReturn(RestStatus.OK);

        // Mock issues aggregation
        ParsedStringTerms issuesTerms = Mockito.mock(ParsedStringTerms.class);
        when(issuesTerms.getBuckets()).thenReturn(new ArrayList<>());
        Aggregations aggregations = Mockito.mock(Aggregations.class);
        when(aggregations.get("label_issues")).thenReturn(issuesTerms);
        when(issuesResponse.getAggregations()).thenReturn(aggregations);

        // Mock search request
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(issuesResponse);

        // Instantiate LabelMetrics
        LabelMetrics labelMetrics = new LabelMetrics();

        // Call method under test
        Map<String, Long> labelIssues = labelMetrics.getLabelIssues("repo", openSearchUtil);

        // Assertions
        assertEquals(new HashMap<>(), labelIssues); // Modify expected result according to your logic
    }

    @Test
    public void testGetLabelPulls() throws IOException {
        // Mock OpenSearchUtil
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);

        // Mock search responses
        SearchResponse pullsResponse = Mockito.mock(SearchResponse.class);
        when(pullsResponse.status()).thenReturn(RestStatus.OK);

        // Mock pulls aggregation
        ParsedStringTerms pullsTerms = Mockito.mock(ParsedStringTerms.class);
        when(pullsTerms.getBuckets()).thenReturn(new ArrayList<>());
        Aggregations aggregations = Mockito.mock(Aggregations.class);
        when(aggregations.get("pull_issues")).thenReturn(pullsTerms);
        when(pullsResponse.getAggregations()).thenReturn(aggregations);

        // Mock search request
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(pullsResponse);

        // Instantiate LabelMetrics
        LabelMetrics labelMetrics = new LabelMetrics();

        // Call method under test
        Map<String, Long> labelPulls = labelMetrics.getLabelPulls("repo", openSearchUtil);

        // Assertions
        assertEquals(new HashMap<>(), labelPulls); // Modify expected result according to your logic
    }

    // Add more test cases as needed
}