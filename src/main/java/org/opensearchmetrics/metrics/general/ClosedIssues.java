package org.opensearchmetrics.metrics.general;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import javax.inject.Inject;

public class ClosedIssues implements Metrics {


    @Inject
    public ClosedIssues() {}

    @Override
    public String toString() {
        return "Closed Issues";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "closed"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        return boolQueryBuilder;
    }

    @Override
    public String searchIndex() {
        return "github_issues";
    }
}
