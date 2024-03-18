package org.opensearchmetrics.metrics.general;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import javax.inject.Inject;

public class OpenPullRequests implements Metrics {


    @Inject
    public OpenPullRequests() {}

    @Override
    public String toString() {
        return "Open Pull Requests";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        return boolQueryBuilder;
    }

    @Override
    public String searchIndex() {
        return "github_pulls";
    }
}
