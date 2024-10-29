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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MetricsLambdaTest {
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
        MetricsLambda metricsLambda = new MetricsLambda(openSearchUtil, metricsCalculation);
        SearchResponse searchResponse = mock(SearchResponse.class);
        Context context = mock(Context.class);
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        Aggregations aggregations = mock(Aggregations.class);
        when(searchResponse.getAggregations()).thenReturn(aggregations);
        ParsedStringTerms termsAggregation = mock(ParsedStringTerms.class);
        when(aggregations.get("repos")).thenReturn(termsAggregation);
        when(termsAggregation.getBuckets()).thenReturn(Collections.emptyList());
        metricsLambda.handleRequest(null, context);
        verify(openSearchUtil, times(1)).search(any(SearchRequest.class));
        verify(metricsCalculation, times(1)).generateGeneralMetrics(anyList());
        verify(metricsCalculation, times(1)).generateLabelMetrics(anyList());
        verify(metricsCalculation, times(1)).generateReleaseMetrics();
        verify(metricsCalculation, times(1)).generateCodeCovMetrics();
    }



    @Test
    public void testHandleRequestWithMetricsCalculationException() {
        MetricsLambda metricsLambda = new MetricsLambda(openSearchUtil, metricsCalculation);
        Context context = mock(Context.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        Aggregations aggregations = mock(Aggregations.class);
        ParsedStringTerms termsAggregation = mock(ParsedStringTerms.class); // Mock the ParsedStringTerms object
        when(termsAggregation.getBuckets()).thenReturn(Collections.emptyList()); // Mock behavior of getBuckets()
        when(aggregations.get("repos")).thenReturn(termsAggregation); // Return non-null termsAggregation
        when(searchResponse.getAggregations()).thenReturn(aggregations); // Return non-null aggregations
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        doThrow(new RuntimeException("Error running Metrics Calculation")).when(metricsCalculation).generateGeneralMetrics(anyList());
        try {
            metricsLambda.handleRequest(null, context);
            fail("Expected a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            System.out.println("Caught exception message: " + e.getMessage());
            assertTrue(e.getMessage().contains("Error running Metrics Calculation"));
        }
    }

}
