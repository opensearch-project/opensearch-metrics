package org.opensearchhealth.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchhealth.health.model.HealthRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public interface Factors {
    BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate);
    SearchRequest createSearchRequest(HealthRequest request, BoolQueryBuilder queryBuilder);

    long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException;

    String getFullName();

    String getDescription();

    String getFactorStringValue(long factorStringValue);
    Map<String, Long> performSearchMapValue(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException;

}
