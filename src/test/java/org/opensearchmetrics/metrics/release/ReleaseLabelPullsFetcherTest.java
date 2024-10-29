/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.apache.lucene.search.TotalHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.search.SearchHits;
import org.opensearchmetrics.util.OpenSearchUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReleaseLabelPullsFetcherTest {

    @Mock
    private OpenSearchUtil openSearchUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReleaseLabelPulls() {
        // Mock search hits
        SearchHits searchHits = Mockito.mock(SearchHits.class);
        TotalHits totalHits = new TotalHits(5, TotalHits.Relation.EQUAL_TO);
        Mockito.when(searchHits.getTotalHits()).thenReturn(totalHits);

        // Mock search response
        SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
        Mockito.when(searchResponse.status()).thenReturn(RestStatus.OK);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);

        // Mock OpenSearchUtil behavior
        Mockito.when(openSearchUtil.search(Mockito.any(SearchRequest.class))).thenReturn(searchResponse);

        // Create an instance of the class containing releaseLabelPulls method
        ReleaseLabelPullsFetcher releaseLabelPullsFetcher = new ReleaseLabelPullsFetcher();

        // Call the method under test
        Long result = releaseLabelPullsFetcher.releaseLabelPulls("1.0", "yourRepo", "open", openSearchUtil);

        // Verify behavior and assertions
        assertEquals(5L, result);
    }
}
