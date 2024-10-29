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
public class UnlabelledIssues implements Metrics {


    @Inject
    public UnlabelledIssues() {}

    @Override
    public String toString() {
        return "Unlabelled Issues";
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(String repo) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("issue_labels.keyword")));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        return boolQueryBuilder;
    }

    @Override
    public String searchIndex() {
        return "github_issues";
    }
}
