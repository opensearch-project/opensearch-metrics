package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearchmetrics.util.OpenSearchUtil;
import org.opensearchmetrics.util.S3Util;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class GithubEventsLambdaTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OpenSearchUtil openSearchUtil;

    @Mock
    private S3Util s3Util;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleRequest() throws JsonProcessingException {
        // Arrange
        ObjectMapper realMapper = new ObjectMapper();
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, realMapper);
        String eventJson = "{\"id\":\"123\",\"name\":\"push\",\"payload\":{\"repository\":{\"name\":\"myrepo\"},\"organization\":{\"login\":\"myorg\"},\"action\":\"created\",\"sender\":{\"login\":\"user\"}},\"uploaded_at\":\"2023-05-01T12:00:00Z\"}";
        List<String> objectKeys = List.of("test_s3_key");
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(s3Util.listObjectsKeys(anyString())).thenReturn(objectKeys);
        when(s3Util.getObjectInputStream(anyString())).thenReturn(new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(eventJson.getBytes())));

        // Act
        githubEventsLambda.handleRequest(null, mock(Context.class));

        // Assert
        verify(openSearchUtil).createIndexIfNotExists("github-events");
        verify(openSearchUtil).bulkIndex(eq("github-events"), any(Map.class));
    }

    @Test
    public void testHandleRequestException() throws IOException {
        // Arrange
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, objectMapper);
        String eventJson = "{\"id\":\"123\",\"name\":\"push\",\"payload\":\"repository\":{\"name\":\"myrepo\"},\"organization\":{\"login\":\"myorg\"},\"action\":\"created\",\"sender\":{\"login\":\"user\"}},\"uploaded_at\":\"2023-05-01T12:00:00Z\"}";
        List<String> objectKeys = List.of("test_s3_key");
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(s3Util.listObjectsKeys(anyString())).thenReturn(objectKeys);
        when(s3Util.getObjectInputStream(anyString())).thenReturn(new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(eventJson.getBytes())));
        doThrow(new RuntimeException("Error running Github Events Lambda")).when(objectMapper).readTree(any(InputStream.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                githubEventsLambda.handleRequest(null, mock(Context.class))
        );
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }
}
