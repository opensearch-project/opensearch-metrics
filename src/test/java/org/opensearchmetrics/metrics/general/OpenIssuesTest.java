package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpenIssuesTest {


    @Test
    void testConstructor() {
        // Create an instance of OpenIssues
        OpenIssues openIssues = new OpenIssues();

        // Ensure the instance is not null
        assertEquals("Open Issues", openIssues.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of OpenIssues
        OpenIssues openIssues = new OpenIssues();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = openIssues.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("state.keyword", "open"))
                .must(QueryBuilders.matchQuery("issue_pull_request", false));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of OpenIssues
        OpenIssues openIssues = new OpenIssues();

        // Call searchIndex method
        String index = openIssues.searchIndex();

        // Verify the returned index
        assertEquals("github_issues", index);
    }

    @Test
    void testToString() {
        // Create an instance of OpenIssues
        OpenIssues openIssues = new OpenIssues();

        // Call toString method
        String result = openIssues.toString();

        // Verify the returned string
        assertEquals("Open Issues", result);
    }
}
