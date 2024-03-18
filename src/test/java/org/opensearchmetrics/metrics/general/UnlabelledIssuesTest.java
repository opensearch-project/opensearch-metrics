package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnlabelledIssuesTest {

    @Test
    void testGetBoolQueryBuilder() {
        // Create instance of UnlabelledIssues
        UnlabelledIssues unlabelledIssues = new UnlabelledIssues();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = unlabelledIssues.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("issue_labels.keyword")))
                .must(QueryBuilders.matchQuery("state.keyword", "open"))
                .must(QueryBuilders.matchQuery("issue_pull_request", false));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testToString() {
        // Create instance of UnlabelledIssues
        UnlabelledIssues unlabelledIssues = new UnlabelledIssues();

        // Call toString method
        String result = unlabelledIssues.toString();

        // Verify the returned string
        assertEquals("Unlabelled Issues", result);
    }

    @Test
    void testSearchIndex() {
        // Create instance of UnlabelledIssues
        UnlabelledIssues unlabelledIssues = new UnlabelledIssues();

        // Call searchIndex method
        String index = unlabelledIssues.searchIndex();

        // Verify the returned index
        assertEquals("github_issues", index);
    }
}
