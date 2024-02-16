package org.opensearchhealth.metrics.repogithubmetrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.dagger.DaggerServiceComponent;
import org.opensearchhealth.dagger.ServiceComponent;
import org.opensearchhealth.metrics.Factors;
import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public enum GitHubFactorThresholds implements Factors {


    UNTRIAGED_ISSUES("Untriaged issues", "The total number of issues labeled as untriaged in the repository."),
    GITHUB_AUDIT("GitHub Audit", "Repository Security Audit Status: 1 means non-compliant, and 0 indicates compliant."),
    UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS("Untriaged issues greater than 30 days", "The total number of issues labeled as untriaged in the repository that are older than 30 days."),

    PRS_NOT_RESPONDED("Pull Requests not responded", "The total number of pull requests in the repository that have not received any comments or responses."),
    ISSUES_NOT_RESPONDED_THIRTY_DAYS("Issues not responded for 30 days", "The total number of issues in the repository that are older than 30 days and have received no comments or responses."),
    PRS_NOT_RESPONDED_THIRTY_DAYS("Pull Requests not responded for 30 days", "The total number of pull requests in the repository that are older than 30 days and have not received any comments or responses.");

    private final String fullName;
    private final String description;
    final ServiceComponent COMPONENT = DaggerServiceComponent.create();

    GitHubFactorThresholds(String fullName, String description) {

        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(MetricsRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        switch (this) {
            case GITHUB_AUDIT:
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                }
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").from("now-30d/d").to("now-29d/d"));
                }
                return boolQueryBuilder;
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.filter(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.filter(QueryBuilders.matchQuery("comments", 0));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").from("now-30d/d").to("now-29d/d"));
                }
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments", 0));
                }
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments", 0));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").from("now-30d/d").to("now-29d/d"));
                }
                return boolQueryBuilder;
            default:
                throw new RuntimeException("Unknown query for Github thresholds");
        }
    }

    @Override
    public SearchRequest createSearchRequest(MetricsRequest request, BoolQueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        switch (this) {
            case GITHUB_AUDIT:
                return searchRequest;
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED:
                searchSourceBuilder.query(queryBuilder);
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown search request for Github thresholds");
        }
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case UNTRIAGED_ISSUES:
                if (status == RestStatus.OK) {
                    return (Math.round(0.05 * searchResponse.getHits().getTotalHits().value));
                }
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                if (status == RestStatus.OK) {
                    return (Math.round(0.03 * searchResponse.getHits().getTotalHits().value));
                }
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED:
                if (status == RestStatus.OK) {
                    return (Math.round(0.02 * searchResponse.getHits().getTotalHits().value));
                }
            case  GITHUB_AUDIT:
                return 0;
            default:
                throw new RuntimeException("Unknown search for Github thresholds");
        }
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFactorStringValue(long factorValue) {
        switch (this) {
            default:
                return null;
        }
    }

    @Override
    public Map<String, Long> performSearchMapValue(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        switch (this) {
            default:
                return null;
        }
    }
}
