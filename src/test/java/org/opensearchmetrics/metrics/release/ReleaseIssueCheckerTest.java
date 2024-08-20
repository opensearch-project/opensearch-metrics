package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReleaseIssueCheckerTest {

    @Mock
    private OpenSearchUtil openSearchUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReleaseOwners() {
        Terms.Bucket bucket1 = mock(Terms.Bucket.class);
        when(bucket1.getKeyAsString()).thenReturn("sample_user_1");
        Terms.Bucket bucket2 = mock(Terms.Bucket.class);
        when(bucket2.getKeyAsString()).thenReturn("sample_user_2");
        Terms.Bucket bucket3 = mock(Terms.Bucket.class);
        when(bucket3.getKeyAsString()).thenReturn("sample_user_3");
        Terms.Bucket bucket4 = mock(Terms.Bucket.class);
        when(bucket4.getKeyAsString()).thenReturn("sample_user_4");
        List<Terms.Bucket> buckets = Arrays.asList(bucket1, bucket2, bucket3, bucket4);
        Terms termsAgg = mock(Terms.class);
        when(termsAgg.getBuckets()).thenAnswer(invocation -> buckets);
        Aggregations aggregations = mock(Aggregations.class);
        when(aggregations.get("issue_assignees")).thenReturn(termsAgg);
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.status()).thenReturn(RestStatus.OK);
        when(searchResponse.getAggregations()).thenReturn(aggregations);
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        ReleaseIssueChecker releaseIssueChecker = new ReleaseIssueChecker();
        String[] result = releaseIssueChecker.releaseOwners("2.16.0", "opensearch-build", openSearchUtil);
        String[] expected = {"sample_user_1", "sample_user_2", "sample_user_3", "sample_user_4"};
        assertArrayEquals(expected, result);
    }

    @Test
    void testReleaseIssue() {
        SearchHit hit1 = mock(SearchHit.class);
        when(hit1.getSourceAsMap()).thenReturn(Map.of("html_url", "https://github.com/opensearch-project/opensearch-build/issues/4115"));
        SearchHit hit2 = mock(SearchHit.class);
        when(hit2.getSourceAsMap()).thenReturn(Map.of("html_url", "https://github.com/opensearch-project/opensearch-build/issues/4454"));
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(new SearchHit[]{hit1, hit2});
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.getHits()).thenReturn(searchHits);
        when(searchResponse.status()).thenReturn(RestStatus.OK);
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        ReleaseIssueChecker releaseIssueChecker = new ReleaseIssueChecker();
        String result = releaseIssueChecker.releaseIssue("2.16.0", "opensearch-build", openSearchUtil);
        assertEquals("https://github.com/opensearch-project/opensearch-build/issues/4115", result);
    }
}
