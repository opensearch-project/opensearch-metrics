/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class S3UtilTest {
    @Mock
    private S3Client mockS3Client;

    private S3Util s3Util;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        s3Util = new S3Util(mockS3Client, "test_bucket_name");
    }

    @Test
    public void WHEN_getObjectInputStream_THEN_return_ResponseInputStream() {
        // Arrange
        ResponseInputStream<GetObjectResponse> mockInputStream = mock(ResponseInputStream.class);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockInputStream);

        // Act
        ResponseInputStream<GetObjectResponse> inputStream = s3Util.getObjectInputStream("test_object_key");

        // Assert
        assertNotNull(inputStream);
        assertEquals(inputStream, mockInputStream);
        verify(mockS3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void WHEN_getObjectInputStreamS3Exception_THEN_throw_Exception() {
        // Arrange
        S3Exception mockException = mock(S3Exception.class);
        AwsErrorDetails mockAwsErrorDetails = mock(AwsErrorDetails.class);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenThrow(mockException);
        when(mockException.awsErrorDetails()).thenReturn(mockAwsErrorDetails);
        when(mockAwsErrorDetails.errorMessage()).thenReturn("Test Error Message");

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            s3Util.getObjectInputStream("test_object_key");
        });

        // Assert
        assertEquals("Failed to get object from S3", exception.getMessage());
        assertInstanceOf(S3Exception.class, exception.getCause());
    }

    @Test
    public void WHEN_listObjectKeys_THEN_return_ListObjectKeys() {
        // Arrange
        ListObjectsV2Response page = ListObjectsV2Response.builder()
                .contents(
                        S3Object.builder().key("testPrefix/file1.txt").build(),
                        S3Object.builder().key("testPrefix/file2.txt").build()
                )
                .build();
        ListObjectsV2Iterable listObjectsV2Iterable = mock(ListObjectsV2Iterable.class);
        when(listObjectsV2Iterable.stream()).thenReturn(Stream.of(page));
        when(mockS3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenReturn(listObjectsV2Iterable);

        // Act
        List<String> result = s3Util.listObjectsKeys("test_prefix");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("testPrefix/file1.txt"));
        assertTrue(result.contains("testPrefix/file2.txt"));
    }

    @Test
    public void WHEN_listObjectKeysS3Exception_THEN_throw_Exception() {
        // Arrange
        S3Exception mockException = mock(S3Exception.class);
        AwsErrorDetails mockAwsErrorDetails = mock(AwsErrorDetails.class);
        when(mockS3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class))).thenThrow(mockException);
        when(mockException.awsErrorDetails()).thenReturn(mockAwsErrorDetails);
        when(mockAwsErrorDetails.errorMessage()).thenReturn("Test Error Message");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                s3Util.listObjectsKeys("test_prefix")
        );
        assertEquals("Failed to list object keys from S3", exception.getMessage());
        assertInstanceOf(S3Exception.class, exception.getCause());
    }

}
