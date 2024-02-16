package org.opensearchhealth.metrics.projectgithubmetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.metrics.Factors;
import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public enum ProjectGithubFactorThresholds implements Factors {

    UNTRIAGED_ISSUES("Untriaged issues", "The total number of issues labeled as untriaged in the project."),
    GITHUB_AUDIT("GitHub Audit", "Project audit based on repositories audit status: A status of 1 denotes noncompliance, while 0 denotes compliance."),
    UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS("Untriaged issues greater than 30 days", "The total number of issues labeled as untriaged in the repository that are older than 30 days.");

    private final String fullName;

    private final String description;
    ProjectGithubFactorThresholds(String fullName, String description) {

        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public BoolQueryBuilder getBoolQueryBuilder(MetricsRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        return boolQueryBuilder;
    }

    @Override
    public SearchRequest createSearchRequest(MetricsRequest request, BoolQueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                return 100;
            case GITHUB_AUDIT:
                return 0;
            default:
                throw new RuntimeException("Unknown search for Github thresholds");
        }
    }


    @Override
    public String getFactorStringValue(long factorStringValue) {
        return null;
    }

    @Override
    public Map<String, Long> performSearchMapValue(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        return null;
    }
}
