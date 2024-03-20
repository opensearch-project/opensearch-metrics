package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClosedIssuesTest {

    @Test
    void testConstructor() {
        // Create an instance of ClosedIssues
        ClosedIssues closedIssues = new ClosedIssues();

        // Ensure the instance is not null
        assertEquals("Closed Issues", closedIssues.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of ClosedIssues
        ClosedIssues closedIssues = new ClosedIssues();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = closedIssues.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("state.keyword", "closed"))
                .must(QueryBuilders.matchQuery("issue_pull_request", false));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of ClosedIssues
        ClosedIssues closedIssues = new ClosedIssues();

        // Call searchIndex method
        String index = closedIssues.searchIndex();

        // Verify the returned index
        assertEquals("github_issues", index);
    }

    @Test
    void testToString() {
        ClosedIssues closedIssues = new ClosedIssues();

        String result = closedIssues.toString();

        assertEquals("Closed Issues", result);
    }
}
