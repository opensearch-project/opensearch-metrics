/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ReleaseRepoFetcherTest {

    @Test
    public void testReadUrl() throws IOException {
        String url = "https://example.com";
        String expectedContent = "Test content from URL\n";
        InputStream inputStream = new ByteArrayInputStream(expectedContent.getBytes());
        URL mockedUrl = Mockito.mock(URL.class);
        when(mockedUrl.openStream()).thenReturn(inputStream);
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        when(fetcher.createURL(url)).thenReturn(mockedUrl);
        String actualContent = fetcher.readUrl(url);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void testReleaseRepoExceptionList() {
        // Create an instance of your class containing the method to be tested
        ReleaseRepoFetcher fetcher = new ReleaseRepoFetcher();

        // Call the method under test
        Map<String, String> result = fetcher.releaseRepoExceptionMap();
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("opensearch-build", "opensearch-build");
        expectedMap.put("performance-analyzer-rca", "performance-analyzer-rca");
        expectedMap.put("project-website", "project-website");
        expectedMap.put("documentation-website", "documentation-website");

        // Assert that the result matches the expected list
        assertEquals(expectedMap, result);
    }
    @Test
    public void testCreateURL() {
        String url = "https://example.com";
        ReleaseRepoFetcher fetcher = new ReleaseRepoFetcher();
        URL createdUrl = fetcher.createURL(url);
        assertEquals(url, createdUrl.toString());
    }

    @Test
    public void testParseYaml() {
        String responseBody = "---\n" +
                "schema-version: '1.1'\n" +
                "build:\n" +
                "  name: OpenSearch\n" +
                "  version: 1.3.15\n" +
                "ci:\n" +
                "  image:\n" +
                "    name: opensearchstaging/ci-runner:ci-runner-centos7-opensearch-build-v3\n" +
                "    args: -e JAVA_HOME=/opt/java/openjdk-11\n" +
                "components:\n" +
                "  - name: OpenSearch\n" +
                "    repository: https://github.com/opensearch-project/OpenSearch.git\n" +
                "    ref: tags/1.3.15\n" +
                "    checks:\n" +
                "      - gradle:publish\n" +
                "      - gradle:properties:version\n" +
                "  - name: commonUtils\n" +
                "    repository: https://github.com/opensearch-project/common-utils.git\n" +
                "    ref: tags/1.3.15.0\n" +
                "    checks:\n" +
                "      - gradle:publish\n" +
                "      - gradle:properties:version\n" +
                "    platforms:\n" +
                "      - linux\n" +
                "      - windows\n";
        Map<String, String> repoNames = new HashMap<>();
        ReleaseRepoFetcher fetcher = new ReleaseRepoFetcher();
        fetcher.parseYaml(responseBody, repoNames);
        assertEquals(2, repoNames.size());
        Map<String, String> expectedRepoNames = new HashMap<>();
        expectedRepoNames.put("OpenSearch", "OpenSearch");
        expectedRepoNames.put("common-utils", "commonUtils");
        assertEquals(expectedRepoNames, repoNames);
        // assertEquals(new ArrayList<>(Arrays.asList("OpenSearch", "common-utils")), repoNames);
    }

    @Test
    public void testGetReleaseRepos() {
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        Mockito.doReturn("Test content").when(fetcher).readUrl(Mockito.anyString());
        Map<String, String> repos = fetcher.getReleaseRepos("1.0.0");
        // Default will always have 4 repos part of exception list
        assertEquals(4, repos.size());
    }

    @Test
    public void testGetReleaseRepos_withData() {
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        Mockito.doReturn("Test content").when(fetcher).readUrl(Mockito.anyString());
        Map<String, String> repoNames = new HashMap<>();
        repoNames.put("repoName", "componentName");
        Mockito.doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String responseBody = (String) args[0];
            Map<String, String> map = (Map<String, String>) args[1];
            // Add all entries from repoNames to the provided map
            map.putAll(repoNames);
            return null;
        }).when(fetcher).parseYaml(Mockito.anyString(), Mockito.anyMap());
        Map<String, String> repos = fetcher.getReleaseRepos("1.0.0");
        // Default will always have 4 repos part of exception list
        assertEquals(5, repos.size());
        assertTrue(repos.containsKey("repoName"));
    }

}