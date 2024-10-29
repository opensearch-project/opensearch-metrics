/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReleaseVersionIncrementCheckerTest {
    @Test
    void testReleaseVersionIncrement_OpenSearch() {
        String releaseVersion = "1.0.0";
        String branch = "main";
        ReleaseVersionIncrementChecker checker = new ReleaseVersionIncrementChecker();
        boolean result = checker.releaseVersionIncrement(releaseVersion, "OpenSearch", branch, null, null);
        assertFalse(result);
    }

    @Test
    void testReleaseVersionIncrement_OpenSearchDashboards() throws IOException {
        String releaseVersion = "1.0.0";
        String branch = "main";
        String repo = "OpenSearch-Dashboards";
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        JsonNodeFactory nodeFactory = JsonNodeFactory.instance;
        ObjectNode objectNode = nodeFactory.objectNode();
        objectNode.put("version", releaseVersion);
        Mockito.when(objectMapper.readTree(Mockito.anyString())).thenReturn(objectNode);
        ReleaseVersionIncrementChecker checker = new ReleaseVersionIncrementChecker();
        boolean result = checker.releaseVersionIncrement(releaseVersion, repo, branch, objectMapper, null);
        assertTrue(result);
    }



    @Test
    void testReleaseVersionIncrement_GithubPulls() {
        String releaseVersion = "1.0.0";
        String repo = "some-repo";
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        OpenSearchUtil openSearchUtil = Mockito.mock(OpenSearchUtil.class);
        SearchResponse searchResponse = Mockito.mock(SearchResponse.class);
        SearchHits searchHits = Mockito.mock(SearchHits.class);
        Mockito.when(searchHits.getHits()).thenReturn(new SearchHit[0]);
        Mockito.when(searchResponse.getHits()).thenReturn(searchHits);
        Mockito.when(openSearchUtil.search(Mockito.any())).thenReturn(searchResponse);
        ReleaseVersionIncrementChecker checker = new ReleaseVersionIncrementChecker();
        boolean result = checker.releaseVersionIncrement(releaseVersion, repo, "main", objectMapper, openSearchUtil);
        assertFalse(result);
    }

}
