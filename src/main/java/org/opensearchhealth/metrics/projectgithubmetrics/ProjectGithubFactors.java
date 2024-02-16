package org.opensearchhealth.metrics.projectgithubmetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.metrics.Factors;
import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum ProjectGithubFactors implements Factors {

    UNTRIAGED_ISSUES("Untriaged issues", "The total number of issues labeled as untriaged in the project."),
    GITHUB_AUDIT("GitHub Audit", "Project audit based on repositories audit status: A status of 1 denotes noncompliance, while 0 denotes compliance."),

    PRS_NOT_RESPONDED("Pull Requests not responded", "The total number of pull requests in the project that have not received any comments or responses."),
    UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS("Untriaged issues greater than 30 days", "The total number of issues labeled as untriaged in the repository that are older than 30 days."),

    ISSUES_NOT_RESPONDED_THIRTY_DAYS("Issues older than 30 days and not responded", "The total number of issues in the repository that are older than 30 days and have received no comments or responses."),
    PRS_NOT_RESPONDED_THIRTY_DAYS("Pull Requests older than 30 days and not responded", "The total number of pull requests in the repository that are older than 30 days and have not received any comments or responses.");

    private final String fullName;

    private final String description;
    ProjectGithubFactors(String fullName, String description) {

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
        switch (this) {
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                return boolQueryBuilder;
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("comments", 0));
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES:
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                return boolQueryBuilder;
            case GITHUB_AUDIT:
                boolQueryBuilder.must(QueryBuilders.matchQuery("audit_status.keyword", "noncompliant"));
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("current_date").from("now-1d").to("now"));
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED:
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
                return boolQueryBuilder;
            default:
                throw new RuntimeException("Unknown Github Project Factor to getBoolQueryBuilder");
        }
    }

    @Override
    public SearchRequest createSearchRequest(MetricsRequest request, BoolQueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(9000);
        searchSourceBuilder.query(queryBuilder);
        switch (this) {
            case GITHUB_AUDIT:
            case PRS_NOT_RESPONDED:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                searchSourceBuilder.aggregation(
                        AggregationBuilders.terms("repo_aggregation")
                        .field("repository.keyword")
                        .size(1000)
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown Github Project Factor to createSearchRequest");
        }
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case PRS_NOT_RESPONDED:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case GITHUB_AUDIT:
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                if (status == RestStatus.OK) {
                    return searchResponse.getHits().getTotalHits().value;
                }
            default:
                throw new RuntimeException("Unknown Github Project Factor to performSearch");
        }
    }


    @Override
    public String getFactorStringValue(long factorStringValue) {
        return null;
    }

    @Override
    public Map<String, Long> performSearchMapValue(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        Map<String, Long> factorMapValue = new HashMap<>();
        switch (this) {
            case GITHUB_AUDIT:
                if (status == RestStatus.OK) {
                    Terms repoAggregation = searchResponse.getAggregations().get("repo_aggregation");
                    for (Terms.Bucket bucket : repoAggregation.getBuckets()) {
                        factorMapValue.put(bucket.getKeyAsString(), 1L);
                    }
                }
                if(!factorMapValue.isEmpty()) {
                    return factorMapValue.entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                }
            case UNTRIAGED_ISSUES:
            case PRS_NOT_RESPONDED:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                if (status == RestStatus.OK) {
                    Terms repoAggregation = searchResponse.getAggregations().get("repo_aggregation");
                    for (Terms.Bucket bucket : repoAggregation.getBuckets()) {
                        factorMapValue.put(bucket.getKeyAsString(), bucket.getDocCount());
                    }
                }
                if(!factorMapValue.isEmpty()) {
                    return factorMapValue.entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                }
            default:
                return null;
        }
    }
}
