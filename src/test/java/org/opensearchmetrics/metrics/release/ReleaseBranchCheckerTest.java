package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ReleaseBranchCheckerTest {
    @Test
    void testReleaseBranchExists() throws IOException {
        // Mocking the getUrlResponse method of UrlResponse class
        UrlResponse urlResponseMock = mock(UrlResponse.class);
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        when(connectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(urlResponseMock.getUrlResponse(anyString())).thenReturn(connectionMock);

        ReleaseBranchChecker releaseBranchChecker = new ReleaseBranchChecker(urlResponseMock);

        assertTrue(releaseBranchChecker.releaseBranch("1.0", "testRepo"));
    }

    @Test
    void testReleaseBranchDoesNotExist() throws IOException {
        // Mocking the getUrlResponse method of UrlResponse class
        UrlResponse urlResponseMock = mock(UrlResponse.class);
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        when(connectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(urlResponseMock.getUrlResponse(anyString())).thenReturn(connectionMock);

        ReleaseBranchChecker releaseBranchChecker = new ReleaseBranchChecker(urlResponseMock);

        assertFalse(releaseBranchChecker.releaseBranch("1.0", "testRepo"));
    }
}
