package org.opensearchmetrics.metrics.general;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.metrics.Sum;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;

public class IssueNegativeReactions implements Metrics {


    @Inject
    public IssueNegativeReactions() {}

    @Override
    public String toString() {
        return "Issue Negative Reactions";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        return boolQueryBuilder;
    }

    @Override
    public String searchIndex() {
        return "github_issues";
    }

    @Override
    public SearchRequest createSearchRequest(BoolQueryBuilder queryBuilder, String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(9000);
        searchSourceBuilder.aggregation(
                AggregationBuilders.sum("total_reactions_sum").field("reactions_minus")
        );
        searchRequest.source(searchSourceBuilder);
        return  searchRequest;
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request) {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            Sum sumAgg = searchResponse.getAggregations().get("total_reactions_sum");
            return (long) sumAgg.getValue();
        } else {
            throw new RuntimeException("Error connecting to the cluster");
        }
    }

}
