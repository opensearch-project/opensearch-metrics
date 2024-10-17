package org.opensearchmetrics.metrics.maintainer;

import org.apache.lucene.search.TotalHits;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import org.opensearchmetrics.model.maintainer.EventData;
import org.opensearchmetrics.model.maintainer.MaintainerData;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        Optional<EventData> latestEvent = maintainerMetrics.queryLatestEvent(testRepo, testUserLogin, testEventType, openSearchUtil);
        EventData testEvent = new EventData();
        testEvent.setEventAction("some_action");
        testEvent.setTimeLastEngaged(Instant.parse("2023-06-15T10:00:00Z"));
        Optional<EventData> testEventOpt = Optional.of(testEvent);


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
        Optional<EventData> latestEvent = maintainerMetrics.queryLatestEvent(testRepo, testUserLogin, testEventType, openSearchUtil);

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

    // Add more test cases as needed
}
