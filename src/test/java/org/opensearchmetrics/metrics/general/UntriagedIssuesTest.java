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

public class UntriagedIssuesTest {

    @Test
    void testGetBoolQueryBuilder() {
        // Create instance of UntriagedIssues
        UntriagedIssues untriagedIssues = new UntriagedIssues();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = untriagedIssues.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"))
                .must(QueryBuilders.matchQuery("state.keyword", "open"))
                .must(QueryBuilders.matchQuery("issue_pull_request", false));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testToString() {
        // Create instance of UntriagedIssues
        UntriagedIssues untriagedIssues = new UntriagedIssues();

        // Call toString method
        String result = untriagedIssues.toString();

        // Verify the returned string
        assertEquals("Untriaged Issues", result);
    }

    @Test
    void testSearchIndex() {
        // Create instance of UntriagedIssues
        UntriagedIssues untriagedIssues = new UntriagedIssues();

        // Call searchIndex method
        String index = untriagedIssues.searchIndex();

        // Verify the returned index
        assertEquals("github_issues", index);
    }
}
