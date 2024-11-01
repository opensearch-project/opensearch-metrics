/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.maintainer;

import org.apache.lucene.search.TotalHits;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.opensearchmetrics.model.maintainer.LatestEventData;
import org.opensearchmetrics.model.maintainer.MaintainerData;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.naming.Context;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MaintainerMetricsTest {

    @Test
    public void testGetEventTypes() {
        // Mock OpenSearchUtil
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);

        // Mock search responses
        SearchResponse eventsResponse = Mockito.mock(SearchResponse.class);
        when(eventsResponse.status()).thenReturn(RestStatus.OK);

        // Mock issues aggregation
        ParsedStringTerms eventTerms = Mockito.mock(ParsedStringTerms.class);
        when(eventTerms.getBuckets()).thenReturn(new ArrayList<>());
        Aggregations aggregations = Mockito.mock(Aggregations.class);
        when(aggregations.get("event_types")).thenReturn(eventTerms);
        when(eventsResponse.getAggregations()).thenReturn(aggregations);

        // Mock search request
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(eventsResponse);

        // Instantiate MaintainerMetrics
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();

        // Call method under test
        List<String> eventTypes = maintainerMetrics.getEventTypes(openSearchUtil);

        // Assertions
        assertEquals(new ArrayList<>(), eventTypes); // Modify expected result according to your logic
    }

    @Test
    public void testQueryLatestEvent() {
        // Mock
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);
        SearchResponse eventsResponse = Mockito.mock(SearchResponse.class);
        SearchHit searchHit = Mockito.mock(SearchHit.class);
        SearchHits searchHits = Mockito.mock(SearchHits.class);
        TopHits topHits = Mockito.mock(TopHits.class);
        Aggregations aggregations = Mockito.mock(Aggregations.class);

        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(eventsResponse);
        when(eventsResponse.status()).thenReturn(RestStatus.OK);
        when(eventsResponse.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("latest_event")).thenReturn(topHits);
        when(topHits.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{searchHit});

        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("action", "some_action");
        sourceMap.put("created_at", "2023-06-15T10:00:00Z");
        when(searchHit.getSourceAsMap()).thenReturn(sourceMap);

        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();

        // Call method under test
        String testRepo = "testRepo";
        String testUserLogin = "testUserLogin";
        String testEventType = "testEventType";
        Optional<LatestEventData> latestEvent = maintainerMetrics.queryLatestEvent(testRepo, testUserLogin, testEventType, openSearchUtil);
        LatestEventData testEvent = new LatestEventData();
        testEvent.setEventType("testEventType");
        testEvent.setEventAction("some_action");
        testEvent.setTimeLastEngaged(Instant.parse("2023-06-15T10:00:00Z"));
        Optional<LatestEventData> testEventOpt = Optional.of(testEvent);


        // Assertions
        assertEquals(testEventOpt, latestEvent); // Modify expected result according to your logic
    }

    @Test
    public void testQueryLatestEventEmpty() {
        // Mock
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);
        SearchResponse eventsResponse = Mockito.mock(SearchResponse.class);
        SearchHits searchHits = Mockito.mock(SearchHits.class);
        TopHits topHits = Mockito.mock(TopHits.class);
        Aggregations aggregations = Mockito.mock(Aggregations.class);

        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(eventsResponse);
        when(eventsResponse.status()).thenReturn(RestStatus.OK);
        when(eventsResponse.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("latest_event")).thenReturn(topHits);
        when(topHits.getHits()).thenReturn(searchHits);
        when(searchHits.getHits()).thenReturn(new SearchHit[0]);

        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();

        // Call method under test
        String testRepo = "testRepo";
        String testUserLogin = "testUserLogin";
        String testEventType = "testEventType";
        Optional<LatestEventData> latestEvent = maintainerMetrics.queryLatestEvent(testRepo, testUserLogin, testEventType, openSearchUtil);

        // Assertions
        assertEquals(Optional.empty(), latestEvent); // Modify expected result according to your logic
    }

    @Test
    public void testMostAndLeastRepoEventCounts() {
        // Mock
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);
        SearchResponse eventsResponse = Mockito.mock(SearchResponse.class);
        Aggregations aggregations = Mockito.mock(Aggregations.class);
        Terms termsLeast = Mockito.mock(Terms.class);
        Terms termsMost = Mockito.mock(Terms.class);
        Terms.Bucket leastBucket = Mockito.mock(Terms.Bucket.class);
        Terms.Bucket mostBucket = Mockito.mock(Terms.Bucket.class);
        List<Terms.Bucket> leastBuckets = Arrays.asList(leastBucket);
        List<Terms.Bucket> mostBuckets = Arrays.asList(mostBucket);


        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(eventsResponse);
        when(eventsResponse.status()).thenReturn(RestStatus.OK);
        when(eventsResponse.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("least_event_count")).thenReturn(termsLeast);
        when(aggregations.get("most_event_count")).thenReturn(termsMost);
        when(termsLeast.getBuckets()).thenAnswer(invocation -> leastBuckets);
        when(termsMost.getBuckets()).thenAnswer(invocation -> mostBuckets);
        when(leastBucket.getDocCount()).thenReturn(10L);
        when(mostBucket.getDocCount()).thenReturn(100L);


        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();

        // Call method under test
        long[] mostAndLeastRepoEventCount = maintainerMetrics.mostAndLeastRepoEventCounts(openSearchUtil);

        // Assertions
        assertArrayEquals(new long[]{100L, 10L}, mostAndLeastRepoEventCount); // Modify expected result according to your logic
    }

    @Test
    public void testRepoEventCount() {
        // Mock
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);
        SearchResponse eventsResponse = Mockito.mock(SearchResponse.class);
        SearchHits searchHits = Mockito.mock(SearchHits.class);

        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(eventsResponse);
        when(eventsResponse.status()).thenReturn(RestStatus.OK);
        when(eventsResponse.getHits()).thenReturn(searchHits);
        when(searchHits.getTotalHits()).thenReturn(new TotalHits(5L, TotalHits.Relation.EQUAL_TO));

        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();

        // Call method under test
        String testRepo = "testRepo";
        long repoEventCount = maintainerMetrics.repoEventCount(testRepo, openSearchUtil);

        // Assertions
        assertEquals(5L, repoEventCount); // Modify expected result according to your logic
    }

    @Test
    public void testGetSlopeAndIntercept() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        double[] slopeAndIntercept = maintainerMetrics.getSlopeAndIntercept(0, 0, 1, 5);
        assertArrayEquals(new double[]{5, 0}, slopeAndIntercept);
    }

    @Test
    public void testGetSlopeAndInterceptNull() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        double[] slopeAndIntercept = maintainerMetrics.getSlopeAndIntercept(0, 0, 0, 5);
        assertNull(slopeAndIntercept);
    }

    @Test
    public void testInactivityLinEq() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        long y = maintainerMetrics.inactivityLinEq(new double[]{-1, 376}, 50, 90);
        assertEquals(326L, y);
    }

    @Test
    public void testInactivityLinEqNull() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        long y = maintainerMetrics.inactivityLinEq(null, 50, 90);
        assertEquals(90L, y);
    }

    @Test
    public void testInactivityLinEqSlopeInterceptWrong() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        assertThrows(RuntimeException.class, () -> maintainerMetrics.inactivityLinEq(new double[]{-1, 376, 928}, 50, 90));
    }

    @Test
    public void testCalculateInactivity() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        MaintainerMetrics mockMaintainerMetrics = Mockito.mock(MaintainerMetrics.class);
        LatestEventData latestEventData = new LatestEventData();
        latestEventData.setTimeLastEngaged(Instant.now().minus(7, ChronoUnit.DAYS));
        double[] slopeAndIntercept = {-1, 376};
        when(mockMaintainerMetrics.inactivityLinEq(slopeAndIntercept, 50, 90)).thenReturn(326L);
        boolean y = maintainerMetrics.calculateInactivity(50L, slopeAndIntercept, 90, latestEventData);
        assertFalse(y);
    }

    @Test
    public void testRepoMaintainers() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        String expectedContent = "test content\n" +
                "| Maintainer       | GitHub ID                                | Affiliation |\n" +
                "| maintainer | [githubId](https://github.com/githubId) | affiliation      |\n" +
                "## Emeritus Maintainers" +
                "| maintainer | [githubId](https://github.com/githubId) | affiliation      |\n" +
                "line3\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                expectedContent.getBytes(StandardCharsets.UTF_8)
        );

        try (MockedConstruction<URL> mocked = mockConstruction(URL.class, (mockUrl, context) ->
                when(mockUrl.openStream()).thenReturn(inputStream))) {

            List<MaintainerData> maintainerDataList = maintainerMetrics.repoMaintainers("repo");

            List<MaintainerData> expectedList = new ArrayList<>();
            MaintainerData expectedMaintainer = new MaintainerData();
            expectedMaintainer.setRepository("repo");
            expectedMaintainer.setName("maintainer");
            expectedMaintainer.setGithubLogin("githubId");
            expectedMaintainer.setAffiliation("affiliation");
            expectedList.add(expectedMaintainer);

            assertEquals(expectedList, maintainerDataList);
        }
    }

    @Test
    public void testRepoMaintainersFileNotFound() {
        MaintainerMetrics maintainerMetrics = new MaintainerMetrics();
        try (MockedConstruction<URL> mocked = mockConstruction(URL.class, (mockUrl, context) ->
                when(mockUrl.openStream()).thenThrow(new FileNotFoundException()))) {
            List<MaintainerData> maintainerDataList = maintainerMetrics.repoMaintainers("repo");
            assertTrue(maintainerDataList.isEmpty());
        }
    }
}
