/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.metrics.Sum;
import org.opensearch.search.aggregations.metrics.SumAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PullCommentsTest {

    @Test
    void testConstructor() {
        // Create an instance of PullComments
        PullComments pullComments = new PullComments();

        // Ensure the instance is not null
        assertEquals("Pull Comments", pullComments.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of PullComments
        PullComments pullComments = new PullComments();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = pullComments.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .mustNot(QueryBuilders.termsQuery("user_login.keyword",
                        "opensearch-trigger-bot[bot]", "dependabot[bot]", "mend-for-github-com[bot]", "opensearch-ci-bot"));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of PullComments
        PullComments pullComments = new PullComments();

        // Call searchIndex method
        String index = pullComments.searchIndex();

        // Verify the returned index
        assertEquals("github_pulls", index);
    }

    @Test
    void testCreateSearchRequest() {
        // Create an instance of PullComments
        PullComments pullComments = new PullComments();

        // Mock dependencies
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        String index = "testIndex";

        // Call createSearchRequest method
        SearchRequest request = pullComments.createSearchRequest(queryBuilder, index);

        // Verify the created SearchRequest
        assertEquals(index, request.indices()[0]); // Verifying index
        assertEquals(queryBuilder, request.source().query()); // Verifying query builder
        assertEquals(9000, request.source().size()); // Verifying size

        // Verify aggregation
        SearchSourceBuilder searchSourceBuilder = request.source();

        assertTrue(searchSourceBuilder.aggregations().getAggregatorFactories().stream()
                .anyMatch(agg -> agg.getName().equals("total_comments_sum")));

        SumAggregationBuilder sumAggregation = (SumAggregationBuilder) searchSourceBuilder.aggregations().getAggregatorFactories().stream()
                .filter(agg -> agg.getName().equals("total_comments_sum"))
                .findFirst()
                .orElse(null);

        assertNotNull(sumAggregation);
        assertEquals("total_comments_sum", sumAggregation.getName());
        assertEquals("comments", sumAggregation.field());
    }

    @Test
    void testPerformSearch_Success() {
        // Mocking dependencies
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        Aggregations aggregations = mock(Aggregations.class);
        Sum sumAgg = mock(Sum.class);

        // Mocking behavior
        when(searchResponse.status()).thenReturn(RestStatus.OK);
        when(searchResponse.getAggregations()).thenReturn(aggregations);
        when(aggregations.get("total_comments_sum")).thenReturn(sumAgg);
        when(sumAgg.getValue()).thenReturn((double) 10L);
        when(openSearchUtil.search(Mockito.any(SearchRequest.class))).thenReturn(searchResponse);

        // Creating test data
        SearchRequest request = mock(SearchRequest.class);

        // Creating instance of IssueNegativeReactions
        PullComments pullComments = new PullComments();

        // Calling the method under test
        long result = pullComments.performSearch(openSearchUtil, request);

        // Verifying the result
        assertEquals(10L, result);
    }

    @Test
    void testPerformSearch_Failure() throws Exception {
        // Mocking dependencies
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        SearchResponse searchResponse = mock(SearchResponse.class);

        // Mocking behavior
        when(searchResponse.status()).thenReturn(RestStatus.BAD_GATEWAY); // Simulating failure
        when(openSearchUtil.search(Mockito.any(SearchRequest.class))).thenReturn(searchResponse);

        // Creating test data
        SearchRequest request = mock(SearchRequest.class);

        // Creating instance of IssueNegativeReactions
        PullComments pullComments = new PullComments();

        // Verifying the exception is thrown
        assertThrows(RuntimeException.class, () -> pullComments.performSearch(openSearchUtil, request));
    }
}
