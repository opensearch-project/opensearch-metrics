/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearchmetrics.metrics.MetricsCalculation;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class MaintainerInactivityLambdaTest {
    @Mock
    private OpenSearchUtil openSearchUtil;

    @Mock
    private MetricsCalculation metricsCalculation;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleRequest(){
        MaintainerInactivityLambda maintainerInactivityLambda = new MaintainerInactivityLambda(openSearchUtil, metricsCalculation);
        SearchResponse searchResponse = mock(SearchResponse.class);
        Context context = mock(Context.class);
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        Aggregations aggregations = mock(Aggregations.class);
        when(searchResponse.getAggregations()).thenReturn(aggregations);
        ParsedStringTerms termsAggregation = mock(ParsedStringTerms.class);
        when(aggregations.get("repos")).thenReturn(termsAggregation);
        when(termsAggregation.getBuckets()).thenReturn(Collections.emptyList());
        maintainerInactivityLambda.handleRequest(null, context);
        verify(openSearchUtil, times(1)).search(any(SearchRequest.class));
        verify(metricsCalculation, times(1)).generateMaintainerMetrics(anyList());
    }

}
