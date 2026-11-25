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
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReleaseVersionIncrementCheckerTest {
    @Test
    void testReadCatalogVersion() throws IOException {
        String catalog = "[versions]\n"
                + "opensearchprotobufs = \"1.7.0\"\n"
                + "opensearch = \"3.11.0\" # current version\n"
                + "[libraries]\n"
                + "opensearch = \"99.0.0\"\n";
        assertEquals("3.11.0", ReleaseVersionIncrementChecker.readCatalogVersion(new BufferedReader(new StringReader(catalog))));
    }

    @Test
    void testReadCatalogVersionRejectsMissingOrInvalidVersion() {
        String catalog = "[versions]\nopensearch = \"not-a-version\"\n[libraries]\nopensearch = \"3.11.0\"\n";
        assertThrows(IOException.class, () -> ReleaseVersionIncrementChecker.readCatalogVersion(new BufferedReader(new StringReader(catalog))));
    }

    @Test
    void testCatalogAndLegacyVersionSources() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger legacyRequests = new AtomicInteger();
        server.createContext("/current/gradle/libs.versions.toml", exchange -> {
            byte[] body = "[versions]\nopensearch = \"3.11.0\"\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/old/buildSrc/version.properties", exchange -> {
            legacyRequests.incrementAndGet();
            byte[] body = "opensearch = 2.18.1-SNAPSHOT\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/broken/gradle/libs.versions.toml", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.createContext("/broken/buildSrc/version.properties", exchange -> {
            legacyRequests.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            ReleaseVersionIncrementChecker checker = new ReleaseVersionIncrementChecker();
            assertTrue(checker.checkOpenSearchVersionAtUrl("3.11.0", "current", base + "current/"));
            assertFalse(checker.checkOpenSearchVersionAtUrl("3.10.0", "current", base + "current/"));
            assertTrue(checker.checkOpenSearchVersionAtUrl("2.18.1", "old", base + "old/"));
            assertFalse(checker.checkOpenSearchVersionAtUrl("3.11.0", "broken", base + "broken/"));
            assertEquals(1, legacyRequests.get());
        } finally {
            server.stop(0);
        }
    }

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
