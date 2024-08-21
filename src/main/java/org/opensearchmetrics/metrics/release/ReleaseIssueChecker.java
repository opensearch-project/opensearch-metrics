package org.opensearchmetrics.metrics.release;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.util.Arrays;

public class ReleaseIssueChecker {

    @Inject
    public ReleaseIssueChecker() {}

    public String[] releaseOwners(String releaseVersion, String repo, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("title.keyword", "[RELEASE] Release version " + releaseVersion));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(9000);
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("issue_assignees")
                        .field("issue_assignees.keyword")
                        .size(50)
        );
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        Terms termsAgg = searchResponse.getAggregations().get("issue_assignees");
        return termsAgg.getBuckets().stream()
                .map(Terms.Bucket::getKeyAsString)
                .toArray(String[]::new);
    }

    public String releaseIssue(String releaseVersion, String repo, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("title.keyword", "[RELEASE] Release version " + releaseVersion));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(9000);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        SearchHit[] hits = searchResponse.getHits().getHits();
        return Arrays.stream(hits)
                .findFirst()
                .map(hit -> (String) hit.getSourceAsMap().get("html_url"))
                .orElse(null);
    }

}
