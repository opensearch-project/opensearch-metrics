package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnlabelledPullRequestsTest {

    @Test
    void testGetBoolQueryBuilder() {
        // Create instance of UnlabelledPullRequests
        UnlabelledPullRequests unlabelledPullRequests = new UnlabelledPullRequests();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = unlabelledPullRequests.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("pull_labels.keyword")))
                .must(QueryBuilders.matchQuery("state.keyword", "open"));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testToString() {
        // Create instance of UnlabelledPullRequests
        UnlabelledPullRequests unlabelledPullRequests = new UnlabelledPullRequests();

        // Call toString method
        String result = unlabelledPullRequests.toString();

        // Verify the returned string
        assertEquals("Unlabelled Pull Requests", result);
    }

    @Test
    void testSearchIndex() {
        // Create instance of UnlabelledPullRequests
        UnlabelledPullRequests unlabelledPullRequests = new UnlabelledPullRequests();

        // Call searchIndex method
        String index = unlabelledPullRequests.searchIndex();

        // Verify the returned index
        assertEquals("github_pulls", index);
    }
}
