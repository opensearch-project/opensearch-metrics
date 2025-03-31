/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
    public void testHandleRequestYesterday() {
        // Arrange
        ObjectMapper realMapper = new ObjectMapper();
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, realMapper);
        String eventJson = "{\"id\":\"123\",\"name\":\"push\",\"payload\":{\"repository\":{\"name\":\"myrepo\"},\"organization\":{\"login\":\"myorg\"},\"action\":\"created\",\"sender\":{\"login\":\"user\"}},\"uploaded_at\":\"2023-05-01T12:00:00Z\"}";
        List<String> objectKeys = List.of("test_s3_key");
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(s3Util.listObjectsKeys(anyString())).thenReturn(objectKeys);
        when(s3Util.getObjectInputStream(anyString())).thenReturn(new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(eventJson.getBytes())));

        Map<String,String> input = new HashMap<>();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        input.put("collectionStartDate", yesterday.toString());

        // Act
        githubEventsLambda.handleRequest(input, mock(Context.class));

        // Assert
        String indexNameYesterday = "github-user-activity-events-" + yesterday.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String indexNameToday = "github-user-activity-events-" + today.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameYesterday, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameToday, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameYesterday), any(Map.class));
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameToday), any(Map.class));
    }

    @Test
    public void testHandleRequestMonthAgo() {
        // Arrange
        ObjectMapper realMapper = new ObjectMapper();
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, realMapper);
        String eventJson = "{\"id\":\"123\",\"name\":\"push\",\"payload\":{\"repository\":{\"name\":\"myrepo\"},\"organization\":{\"login\":\"myorg\"},\"action\":\"created\",\"sender\":{\"login\":\"user\"}},\"uploaded_at\":\"2023-05-01T12:00:00Z\"}";
        List<String> objectKeys = List.of("test_s3_key");
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(s3Util.listObjectsKeys(anyString())).thenReturn(objectKeys);
        when(s3Util.getObjectInputStream(anyString())).thenReturn(new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(eventJson.getBytes())));

        Map<String,String> input = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate lastMonth = today.minus(1, ChronoUnit.MONTHS);
        input.put("collectionStartDate", lastMonth.toString());

        // Act
        githubEventsLambda.handleRequest(input, mock(Context.class));

        // Assert
        String indexNameLastMonth = "github-user-activity-events-" + lastMonth.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameLastMonth, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameLastMonth), any(Map.class));

        String indexNameThisMonth = "github-user-activity-events-" + today.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameThisMonth, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameThisMonth), any(Map.class));
    }

    @Test
    public void testHandleRequestDefault() {
        // Arrange
        ObjectMapper realMapper = new ObjectMapper();
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, realMapper);
        String eventJson = "{\"id\":\"123\",\"name\":\"push\",\"payload\":{\"repository\":{\"name\":\"myrepo\"},\"organization\":{\"login\":\"myorg\"},\"action\":\"created\",\"sender\":{\"login\":\"user\"}},\"uploaded_at\":\"2023-05-01T12:00:00Z\"}";
        List<String> objectKeys = List.of("test_s3_key");
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(s3Util.listObjectsKeys(anyString())).thenReturn(objectKeys);
        when(s3Util.getObjectInputStream(anyString())).thenReturn(new ResponseInputStream<>(getObjectResponse, new ByteArrayInputStream(eventJson.getBytes())));

        Map<String,String> input = new HashMap<>();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        // Act
        githubEventsLambda.handleRequest(input, mock(Context.class));

        // Assert
        String indexNameYesterday = "github-user-activity-events-" + yesterday.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        String indexNameToday = "github-user-activity-events-" + today.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameYesterday, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).createIndexIfNotExists(indexNameToday, Optional.empty());
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameYesterday), any(Map.class));
        verify(openSearchUtil, atLeastOnce()).bulkIndex(eq(indexNameToday), any(Map.class));
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

        Map<String,String> input = new HashMap<>();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minus(1, ChronoUnit.DAYS);
        input.put("collectionStartDate", yesterday.toString());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                githubEventsLambda.handleRequest(input, mock(Context.class))
        );
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    public void testHandleRequestDateException() {
        Map<String,String> input = new HashMap<>();
        input.put("collectionStartDate", "l;ajsd;fljk");

        // Arrange
        GithubEventsLambda githubEventsLambda = new GithubEventsLambda(openSearchUtil, s3Util, objectMapper);
        assertThrows(DateTimeParseException.class, () ->
                githubEventsLambda.handleRequest(input, mock(Context.class))
        );
    }
}
