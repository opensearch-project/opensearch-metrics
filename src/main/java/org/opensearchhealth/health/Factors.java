package org.opensearchhealth.health;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.opensearchhealth.health.model.HealthRequest;

import java.io.IOException;
import java.time.LocalDateTime;

public interface Factors {
    BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate);
    SearchRequest createSearchRequest(HealthRequest request, DateHistogramInterval interval,
                                      BoolQueryBuilder queryBuilder, AggregationType aggregationType);

    long performSearch(SearchRequest request, HealthRequest.AggType aggType,
                      AggregationType aggregationType) throws IOException;

    String getFullName();

}
