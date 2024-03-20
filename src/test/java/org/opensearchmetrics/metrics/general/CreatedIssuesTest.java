package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreatedIssuesTest {

    @Test
    void testConstructor() {
        // Create an instance of CreatedIssues
        CreatedIssues createdIssues = new CreatedIssues();

        // Ensure the instance is not null
        assertEquals("Created Issues", createdIssues.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of CreatedIssues
        CreatedIssues createdIssues = new CreatedIssues();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = createdIssues.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("issue_pull_request", false));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of CreatedIssues
        CreatedIssues createdIssues = new CreatedIssues();

        // Call searchIndex method
        String index = createdIssues.searchIndex();

        // Verify the returned index
        assertEquals("github_issues", index);
    }

    @Test
    void testToString() {
        // Create an instance of CreatedIssues
        CreatedIssues createdIssues = new CreatedIssues();

        // Call toString method
        String result = createdIssues.toString();

        // Verify the returned string
        assertEquals("Created Issues", result);
    }
}
