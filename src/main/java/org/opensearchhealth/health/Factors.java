package org.opensearchhealth.health;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchhealth.health.model.HealthRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;

public interface Factors {
    BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate);
    SearchRequest createSearchRequest(HealthRequest request, BoolQueryBuilder queryBuilder);

    long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request) throws IOException;

    String getFullName();

    String getDescription();

    String getFactorStringValue(long factorStringValue);

}
