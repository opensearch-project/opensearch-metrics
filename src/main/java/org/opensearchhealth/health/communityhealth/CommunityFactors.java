package org.opensearchhealth.health.communityhealth;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.dagger.DaggerServiceComponent;
import org.opensearchhealth.dagger.ServiceComponent;
import org.opensearchhealth.health.AggregationType;
import org.opensearchhealth.health.Factors;
import org.opensearchhealth.health.model.HealthRequest;

import java.io.IOException;
import java.time.LocalDateTime;

public enum CommunityFactors implements Factors {


    TOTAL_GITHUB_ISSUES_CREATED("GitHub issues created"),
    TOTAL_GITHUB_PULLS_CREATED("GitHub pulls created"),
    TOTAL_GITHUB_PULLS_MERGED("GitHub pulls merged"),
    GITHUB_OPEN_ISSUES("GitHub open issues"),
    GITHUB_OPEN_PULLS("GitHub open pulls"),
    TOTAL_FORKS("Number of forks"),
    TOTAL_STARS("Number of stars"),
    TOTAl_SUBSCRIBERS("Number of subscribers"),
    TOTAL_SIZE("Repo size");

    private final String fullName;
    final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    CommunityFactors(String fullName) {

        this.fullName = fullName;
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                }
                return boolQueryBuilder;
            case TOTAL_GITHUB_PULLS_CREATED:
            case TOTAL_FORKS:
            case TOTAL_STARS:
            case TOTAl_SUBSCRIBERS:
            case TOTAL_SIZE:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                }
                return boolQueryBuilder;
            case TOTAL_GITHUB_PULLS_MERGED:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("merged", true));
                }
                return boolQueryBuilder;
            case GITHUB_OPEN_ISSUES:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                }
                return boolQueryBuilder;
            case GITHUB_OPEN_PULLS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                }
                return boolQueryBuilder;

            default:
                throw new RuntimeException("Unknown Community Factor to getBoolQueryBuilder");
        }
    }

    @Override
    public SearchRequest createSearchRequest(HealthRequest request, DateHistogramInterval interval, BoolQueryBuilder queryBuilder, AggregationType aggregationType) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
            case TOTAL_GITHUB_PULLS_CREATED:
            case TOTAL_GITHUB_PULLS_MERGED:
            case GITHUB_OPEN_ISSUES:
            case GITHUB_OPEN_PULLS:
            case TOTAL_FORKS:
            case TOTAL_STARS:
            case TOTAl_SUBSCRIBERS:
            case TOTAL_SIZE:
                searchSourceBuilder.query(queryBuilder);
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown Community Factor to createSearchRequest");
        }
    }

    @Override
    public long performSearch(SearchRequest request, HealthRequest.AggType aggType, AggregationType aggregationType) throws IOException {
        SearchResponse searchResponse = COMPONENT.getOpenSearchUtil().search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
            case TOTAL_GITHUB_PULLS_CREATED:
            case TOTAL_GITHUB_PULLS_MERGED:
            case GITHUB_OPEN_ISSUES:
            case GITHUB_OPEN_PULLS:
                if (status == RestStatus.OK) {
                    return searchResponse.getHits().getTotalHits().value;
                }
            case TOTAL_FORKS:
                if (status == RestStatus.OK) {
                    for (SearchHit hit : searchResponse.getHits()) {
                        Object fieldValue = hit.getSourceAsMap().get("forks_count");
                        return Long.valueOf(fieldValue.toString());
                    }
                }
            case TOTAL_STARS:
                if (status == RestStatus.OK) {
                    for (SearchHit hit : searchResponse.getHits()) {
                        Object fieldValue = hit.getSourceAsMap().get("stargazers_count");
                        return Long.valueOf(fieldValue.toString());
                    }
                }
            case TOTAl_SUBSCRIBERS:
                if (status == RestStatus.OK) {
                    for (SearchHit hit : searchResponse.getHits()) {
                        Object fieldValue = hit.getSourceAsMap().get("subscribers_count");;
                        return Long.valueOf(fieldValue.toString());
                    }
                }
            case TOTAL_SIZE:
                if (status == RestStatus.OK) {
                    for (SearchHit hit : searchResponse.getHits()) {
                        Object fieldValue = hit.getSourceAsMap().get("size");
                        return Long.valueOf(fieldValue.toString());
                    }
                }
            default:
                throw new RuntimeException("Unknown Community Factor to performSearch");
        }
    }

    @Override
    public String getFullName() {
        return fullName;
    }
}
