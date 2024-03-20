package org.opensearchmetrics.metrics.general;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import javax.inject.Inject;

public class MergedPullRequests implements Metrics {


    @Inject
    public MergedPullRequests() {}

    @Override
    public String toString() {
        return "Pull Requests Merged";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("merged", true));
        return boolQueryBuilder;
    }

    @Override
    public String searchIndex() {
        return "github_pulls";
    }
}
