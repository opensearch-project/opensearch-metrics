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

public class OpenPullRequestsTest {

    @Test
    void testConstructor() {
        // Create an instance of OpenPullRequests
        OpenPullRequests openPullRequests = new OpenPullRequests();

        // Ensure the instance is not null
        assertEquals("Open Pull Requests", openPullRequests.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of OpenPullRequests
        OpenPullRequests openPullRequests = new OpenPullRequests();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = openPullRequests.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("state.keyword", "open"));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of OpenPullRequests
        OpenPullRequests openPullRequests = new OpenPullRequests();

        // Call searchIndex method
        String index = openPullRequests.searchIndex();

        // Verify the returned index
        assertEquals("github_pulls", index);
    }

    @Test
    void testToString() {
        // Create an instance of OpenPullRequests
        OpenPullRequests openPullRequests = new OpenPullRequests();

        // Call toString method
        String result = openPullRequests.toString();

        // Verify the returned string
        assertEquals("Open Pull Requests", result);
    }
}
