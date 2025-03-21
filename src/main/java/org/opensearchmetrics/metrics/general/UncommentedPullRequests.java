/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.general;

import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import javax.inject.Inject;

public class UncommentedPullRequests implements Metrics {

    @Inject
    public UncommentedPullRequests() {}

    @Override
    public String toString() {
        return "Uncommented Pull Requests";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        return boolQueryBuilder;
    }
    @Override
    public String searchIndex() {
        return "github_pulls";
    }
}
