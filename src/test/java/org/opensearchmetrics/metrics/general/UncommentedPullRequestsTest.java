/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UncommentedPullRequestsTest {

    @Test
    void testGetBoolQueryBuilder() {
        // Create instance of UncommentedPullRequests
        UncommentedPullRequests uncommentedPullRequests = new UncommentedPullRequests();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = uncommentedPullRequests.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("comments", 0))
                .must(QueryBuilders.matchQuery("state.keyword", "open"));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testToString() {
        // Create instance of UncommentedPullRequests
        UncommentedPullRequests uncommentedPullRequests = new UncommentedPullRequests();

        // Call toString method
        String result = uncommentedPullRequests.toString();

        // Verify the returned string
        assertEquals("Uncommented Pull Requests", result);
    }

    @Test
    void testSearchIndex() {
        // Create instance of UncommentedPullRequests
        UncommentedPullRequests uncommentedPullRequests = new UncommentedPullRequests();

        // Call searchIndex method
        String index = uncommentedPullRequests.searchIndex();

        // Verify the returned index
        assertEquals("github_pulls", index);
    }
}
