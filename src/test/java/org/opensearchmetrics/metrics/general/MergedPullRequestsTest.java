package org.opensearchmetrics.metrics.general;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergedPullRequestsTest {

    @Test
    void testConstructor() {
        // Create an instance of MergedPullRequests
        MergedPullRequests mergedPullRequests = new MergedPullRequests();

        // Ensure the instance is not null
        assertEquals("Pull Requests Merged", mergedPullRequests.toString());
    }

    @Test
    void testGetBoolQueryBuilder() {
        // Create an instance of MergedPullRequests
        MergedPullRequests mergedPullRequests = new MergedPullRequests();

        // Call getBoolQueryBuilder with a sample repository name
        BoolQueryBuilder queryBuilder = mergedPullRequests.getBoolQueryBuilder("sampleRepo");

        // Verify the generated query
        BoolQueryBuilder expectedQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("repository.keyword", "sampleRepo"))
                .must(QueryBuilders.matchQuery("merged", true));

        assertEquals(expectedQueryBuilder.toString(), queryBuilder.toString());
    }

    @Test
    void testSearchIndex() {
        // Create an instance of MergedPullRequests
        MergedPullRequests mergedPullRequests = new MergedPullRequests();

        // Call searchIndex method
        String index = mergedPullRequests.searchIndex();

        // Verify the returned index
        assertEquals("github_pulls", index);
    }

    @Test
    void testToString() {
        // Create an instance of MergedPullRequests
        MergedPullRequests mergedPullRequests = new MergedPullRequests();

        // Call toString method
        String result = mergedPullRequests.toString();

        // Verify the returned string
        assertEquals("Pull Requests Merged", result);
    }
}
