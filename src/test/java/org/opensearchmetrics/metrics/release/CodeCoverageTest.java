/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.metrics.release;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearchmetrics.model.codecov.CodeCovResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodeCoverageTest {

    @Mock
    private CloseableHttpClient httpClient;

    private CodeCoverage codeCoverage;

    private final String validJsonResponse = """
        {
            "results": [{
                "state": "complete",
                "commitid": "abc123",
                "totals": {
                    "coverage": 85.5
                }
            }]
        }
        """;

    private final String emptyResultsResponse = """
        {
            "results": []
        }
        """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        codeCoverage = new CodeCoverage(httpClient);
    }

    @Test
    void testDefaultConstructor() {
        CodeCoverage defaultCodeCoverage = new CodeCoverage();
        assertNotNull(defaultCodeCoverage);
    }

    @Test
    void testSuccessfulCoverageRequest() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(validJsonResponse.getBytes()));
        when(httpClient.execute(any())).thenReturn(httpResponse);
        CodeCovResponse response = codeCoverage.coverage("main", "test-repo");
        assertNotNull(response);
        assertEquals("complete", response.getState());
        assertEquals("abc123", response.getCommitId());
        assertEquals(85.5, response.getCoverage());
        assertEquals("https://api.codecov.io/api/v2/github/opensearch-project/repos/test-repo/commits?branch=main",
                response.getUrl());
    }

    @Test
    void testEmptyResultsResponse() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = mock(HttpEntity.class);

        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(emptyResultsResponse.getBytes()));
        when(httpClient.execute(any())).thenReturn(httpResponse);
        CodeCovResponse response = codeCoverage.coverage("main", "test-repo");
        assertNotNull(response);
        assertEquals("no-coverage", response.getState());
        assertEquals("none", response.getCommitId());
        assertEquals(0.0, response.getCoverage());
    }

    @Test
    void testWithout200Response() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(404);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any())).thenReturn(httpResponse);
        CodeCovResponse response = codeCoverage.coverage("main", "test-repo");
        assertNotNull(response);
        assertNull(response.getState());
        assertNull(response.getCommitId());
        assertEquals(null, response.getCoverage());
    }

    @Test
    void testEntityUtilsThrowsException() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenThrow(new IOException("Error reading content"));
        when(httpClient.execute(any())).thenReturn(httpResponse);
        assertThrows(RuntimeException.class, () -> codeCoverage.coverage("main", "test-repo"));
    }

    @Test
    void testHttpClientExecuteThrowsException() throws IOException {
        when(httpClient.execute(any())).thenThrow(new IOException("Failed to execute request"));
        assertThrows(RuntimeException.class, () -> codeCoverage.coverage("main", "test-repo"));
    }

    @Test
    void testResponseCloseThrowsException() throws IOException {
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(404);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any())).thenReturn(httpResponse);
        doThrow(new IOException("Failed to close response")).when(httpResponse).close();
        assertThrows(RuntimeException.class, () -> codeCoverage.coverage("main", "test-repo"));
    }
}
