/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;

public class ReleaseLabelPullsFetcher {

    @Inject
    public ReleaseLabelPullsFetcher() {}

    public Long releaseLabelPulls(String releaseVersion, String repo, String pullState, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", pullState));
        boolQueryBuilder.must(QueryBuilders.matchQuery("pull_labels.keyword", "v" + releaseVersion));
        SearchRequest searchRequest = new SearchRequest("github_pulls");
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
