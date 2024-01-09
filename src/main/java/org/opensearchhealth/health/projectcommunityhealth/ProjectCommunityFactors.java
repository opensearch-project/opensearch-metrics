package org.opensearchhealth.health.projectcommunityhealth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.metrics.Avg;
import org.opensearch.search.aggregations.metrics.Cardinality;
import org.opensearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.opensearch.search.aggregations.metrics.Sum;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.health.Factors;
import org.opensearchhealth.health.model.HealthRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public enum ProjectCommunityFactors implements Factors {

    NUMBER_OF_CONTRIBUTORS("Project Contributors", "Total number of contributors for OpenSearch Project."),
    GITHUB_OPEN_ISSUES("GitHub open issues", "The total number of issues that are currently in the open state."),
    GITHUB_CLOSED_ISSUES("GitHub closed issues", "The total number of issues that are currently in the closed state."),
    GITHUB_OPEN_PULLS("GitHub open pulls", "The total number of pull requests that are currently in the open state."),

    TOTAL_GITHUB_PULLS_MERGED("GitHub pulls merged", "The total number of pull requests merged for the project."),
    TOTAL_GITHUB_ISSUES_CREATED("GitHub issues created", "The total number of issues created for the project."),
    TOTAL_GITHUB_PULLS_CREATED("GitHub pulls created", "The total number of pull requests created for the project."),

    TOTAL_POSITIVE_REACTIONS_ISSUES("Issues Positive Reactions", "The total number of positive reactions on issues in the project."),
    TOTAL_NEGATIVE_REACTIONS_ISSUES("Issue Negative Reactions", "The total number of negative reactions on issues in the project."),

    ISSUES_AVG_TIME_OPEN("Average Issue Open Time: Measured in Days", "The average duration, measured in days, from the date an issue was created to the current day."),
    ISSUES_AVG_TIME_CLOSE("Average Issue Close Time: Measured in Days", "The average duration, measured in days, from the date an issue was initiated to when it was marked closed."),
    PRS_AVG_TIME_OPEN("Average Pull Request Open Time: Measured in Days", "The average duration, measured in days, from the date a pull request was created to the current day."),
    PRS_AVG_TIME_CLOSE("Average Pull Request Merge Time: Measured in Days", "The average duration, measured in days, from the date a pull request was created to when it was marked merged.");

    private final String fullName;
    private final String description;
    ProjectCommunityFactors(String fullName, String description) {

        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(HealthRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
            case TOTAL_POSITIVE_REACTIONS_ISSUES:
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                return boolQueryBuilder;
            case TOTAL_GITHUB_PULLS_CREATED:
            case NUMBER_OF_CONTRIBUTORS:
            case TOTAL_NEGATIVE_REACTIONS_ISSUES:
                return boolQueryBuilder;
            case TOTAL_GITHUB_PULLS_MERGED:
            case PRS_AVG_TIME_CLOSE:
                boolQueryBuilder.must(QueryBuilders.existsQuery("merged_at"));
                return boolQueryBuilder;
            case GITHUB_OPEN_ISSUES:
            case ISSUES_AVG_TIME_OPEN:
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                return boolQueryBuilder;
            case ISSUES_AVG_TIME_CLOSE:
            case GITHUB_CLOSED_ISSUES:
                boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "closed"));
                return boolQueryBuilder;
            case GITHUB_OPEN_PULLS:
            case PRS_AVG_TIME_OPEN:
                boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                return boolQueryBuilder;


            default:
                throw new RuntimeException("Unknown Project Community Factor to getBoolQueryBuilder");
        }
    }

    @Override
    public SearchRequest createSearchRequest(HealthRequest request, BoolQueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
            case TOTAL_GITHUB_PULLS_CREATED:
            case TOTAL_GITHUB_PULLS_MERGED:
            case GITHUB_OPEN_ISSUES:
            case GITHUB_CLOSED_ISSUES:
            case GITHUB_OPEN_PULLS:
                searchSourceBuilder.size(9000);
                searchRequest.scroll(TimeValue.timeValueMinutes(2));
                searchSourceBuilder.query(queryBuilder);
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case TOTAL_POSITIVE_REACTIONS_ISSUES:
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.aggregation(
                        AggregationBuilders.sum("sum_field").field("reactions_plus")
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case TOTAL_NEGATIVE_REACTIONS_ISSUES:
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.aggregation(
                        AggregationBuilders.sum("sum_field").field("reactions_minus")
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case NUMBER_OF_CONTRIBUTORS:
                searchSourceBuilder.query(queryBuilder);
                CardinalityAggregationBuilder aggregation = AggregationBuilders.cardinality("contributors_unique_keyword_count")
                        .field("login.keyword");
                searchSourceBuilder.aggregation(aggregation);
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case ISSUES_AVG_TIME_OPEN:
            case PRS_AVG_TIME_OPEN:
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.aggregation(
                        AggregationBuilders.avg("avg_open_field").field("time_open_days")
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case ISSUES_AVG_TIME_CLOSE:
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.aggregation(
                        AggregationBuilders.avg("avg_close_field").field("time_to_close_days")
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            case PRS_AVG_TIME_CLOSE:
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.aggregation(
                        AggregationBuilders.avg("avg_close_field").field("time_to_merge_days")
                );
                searchRequest.source(searchSourceBuilder);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown Project Community Factor to createSearchRequest");
        }
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case TOTAL_GITHUB_ISSUES_CREATED:
            case TOTAL_GITHUB_PULLS_CREATED:
            case TOTAL_GITHUB_PULLS_MERGED:
            case GITHUB_OPEN_ISSUES:
            case GITHUB_CLOSED_ISSUES:
            case GITHUB_OPEN_PULLS:
                if (status == RestStatus.OK) {
                    /*long cumulativeTotalHits =  searchResponse.getHits().getTotalHits().value;
                    String scrollId = searchResponse.getScrollId();
                    cumulativeTotalHits = opensearchUtil.processScrolling(scrollId, cumulativeTotalHits);
                    return cumulativeTotalHits;*/
                    return searchResponse.getHits().getTotalHits().value;
                }
            case TOTAL_POSITIVE_REACTIONS_ISSUES:
            case TOTAL_NEGATIVE_REACTIONS_ISSUES:
                Aggregation reactionsAggregation = searchResponse.getAggregations().get("sum_field");
                Sum sumAgg = (Sum) reactionsAggregation;
                long sumValue = (long) sumAgg.getValue();
                return sumValue;
            case NUMBER_OF_CONTRIBUTORS:
                Aggregation contributorsAggregation = searchResponse.getAggregations().get("contributors_unique_keyword_count");
                Cardinality cardinalityAgg = (Cardinality) contributorsAggregation;
                long uniqueCount = cardinalityAgg.getValue();
                return uniqueCount;
            case ISSUES_AVG_TIME_OPEN:
            case PRS_AVG_TIME_OPEN:
                Aggregation openAggregation = searchResponse.getAggregations().get("avg_open_field");
                Avg avgOpenAgg = (Avg) openAggregation;
                long avgOpenValue = (long) avgOpenAgg.getValue();
                return avgOpenValue;
            case ISSUES_AVG_TIME_CLOSE:
            case PRS_AVG_TIME_CLOSE:
                Aggregation closeAggregation = searchResponse.getAggregations().get("avg_close_field");
                Avg avgCloseAgg = (Avg) closeAggregation;
                long avgCloseValue = (long) avgCloseAgg.getValue();
                return avgCloseValue;
            default:
                throw new RuntimeException("Unknown Project Community Factor to performSearch");
        }
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
