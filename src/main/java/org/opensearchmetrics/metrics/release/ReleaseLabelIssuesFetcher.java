package org.opensearchmetrics.metrics.release;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;

public class ReleaseLabelIssuesFetcher {

    @Inject
    public ReleaseLabelIssuesFetcher() {}

    public Long releaseLabelIssues(String releaseVersion, String repo, String issueState, boolean autoCut, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", issueState));
        if(autoCut) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "autocut"));
        }
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "v" + releaseVersion));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(9000);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            return searchResponse.getHits().getTotalHits().value;
        } else
            return 0L;
    }
}
