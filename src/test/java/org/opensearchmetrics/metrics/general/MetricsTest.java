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
import org.opensearchmetrics.util.OpenSearchUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsTest {

    @Test
    void testCreateSearchRequest() {
        // Mocking dependencies
        BoolQueryBuilder queryBuilder = mock(BoolQueryBuilder.class);
        String index = "testIndex";

        // Creating Metrics instance
        Metrics metrics = new OpenIssues(); // Assuming MetricsImpl is an implementation of Metrics

        // Calling the method under test
        SearchRequest request = metrics.createSearchRequest(queryBuilder, index);

        // Verifying the created SearchRequest
        assertEquals(index, request.indices()[0]); // Verifying index
        assertEquals(queryBuilder, request.source().query()); // Verifying query builder
        assertEquals(9000, request.source().size()); // Verifying size
    }


    @Test
    void testPerformSearch_Failure() throws Exception {
        // Mocking dependencies
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        RestStatus restStatus = mock(RestStatus.class);

        // Mocking behavior
        when(searchResponse.status()).thenReturn(RestStatus.BAD_GATEWAY); // Simulating failure
        when(openSearchUtil.search(Mockito.any(SearchRequest.class))).thenReturn(searchResponse);

        // Creating test data
        SearchRequest request = mock(SearchRequest.class);

        // Creating Metrics instance
        Metrics metrics = new OpenIssues(); // Assuming MetricsImpl is an implementation of Metrics

        // Verifying the exception is thrown
        assertThrows(RuntimeException.class, () -> metrics.performSearch(openSearchUtil, request));
    }
}
