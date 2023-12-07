package org.opensearchhealth.health.githubhealth;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.dagger.DaggerServiceComponent;
import org.opensearchhealth.dagger.ServiceComponent;
import org.opensearchhealth.health.AggregationType;
import org.opensearchhealth.health.Factors;
import org.opensearchhealth.health.model.HealthRequest;

import java.io.IOException;
import java.time.LocalDateTime;

public enum GitHubFactors implements Factors {

    UNTRIAGED_ISSUES("Number of Untriaged issues"),
    GITHUB_AUDIT("GitHub Audit"),
    UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS("Number of Untriaged issues greater than 30 days"),

    ISSUES_NOT_RESPONDED_THIRTY_DAYS("Number of ISSUES not responded for 30 days"),
    PRS_NOT_RESPONDED_THIRTY_DAYS("Number of PRs not responded for 30 days");

    private final String fullName;
    final ServiceComponent COMPONENT = DaggerServiceComponent.create();

    GitHubFactors(String fullName) {

        this.fullName = fullName;
    }

    @Override
    public String getFullName() {
        return fullName;
    }


    @Override
    public BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        switch (this) {
            case GITHUB_AUDIT:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("audit_status", "noncompliant"));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery(request.getDateField()).gte("now").lte("now-1d"));
                }
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                }
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery(request.getDateField()).gte("now").lte("now-30d"));
                }
                return boolQueryBuilder;
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments", 0));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery(request.getDateField()).gte("now").lte("now-30d"));
                }
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery(request.getDateField()).gte("now").lte("now-30d"));
                }
                return boolQueryBuilder;

            default:
                throw new RuntimeException("Unknown Community Factor to getBoolQueryBuilder");
        }
    }

    @Override
    public SearchRequest createSearchRequest(HealthRequest request, DateHistogramInterval interval,
                                             BoolQueryBuilder queryBuilder, AggregationType aggregationType) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        switch (this) {
            case GITHUB_AUDIT:
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                searchSourceBuilder.query(queryBuilder);
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown Community Factor to createSearchRequest");
        }
    }

    @Override
    public long performSearch(SearchRequest request, HealthRequest.AggType aggType,
                                         AggregationType aggregationType) throws IOException {
        SearchResponse searchResponse = COMPONENT.getOpenSearchUtil().search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case GITHUB_AUDIT:
                if (status == RestStatus.OK) {
                    if(searchResponse.getHits().getTotalHits().value == 0) {
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                if (status == RestStatus.OK) {
                    return searchResponse.getHits().getTotalHits().value;
                }
            default:
                throw new RuntimeException("Unknown Community Factor to performSearch");
        }
    }

}
