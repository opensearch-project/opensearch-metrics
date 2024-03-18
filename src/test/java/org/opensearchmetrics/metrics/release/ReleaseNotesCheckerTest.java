package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReleaseNotesCheckerTest {

    @Test
    void testReleaseNotesExist() throws IOException {
        // Mocking the getUrlResponse method of UrlResponse class
        UrlResponse urlResponseMock = mock(UrlResponse.class);
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        when(connectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(urlResponseMock.getUrlResponse(anyString())).thenReturn(connectionMock);

        ReleaseNotesChecker releaseNotesChecker = new ReleaseNotesChecker(urlResponseMock);

        assertTrue(releaseNotesChecker.releaseNotes("1.0", "OpenSearch"));
    }

    @Test
    void testReleaseNotesDoNotExist() throws IOException {
        // Mocking the getUrlResponse method of UrlResponse class
        UrlResponse urlResponseMock = mock(UrlResponse.class);
        HttpURLConnection connectionMock = mock(HttpURLConnection.class);
        when(connectionMock.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(urlResponseMock.getUrlResponse(anyString())).thenReturn(connectionMock);

        ReleaseNotesChecker releaseNotesChecker = new ReleaseNotesChecker(urlResponseMock);

        assertFalse(releaseNotesChecker.releaseNotes("1.0", "OpenSearch"));
    }
}
