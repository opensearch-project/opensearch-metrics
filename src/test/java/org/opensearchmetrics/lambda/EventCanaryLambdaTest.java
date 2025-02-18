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
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHFileNotFoundException;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.opensearchmetrics.util.SecretsManagerUtil;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventCanaryLambdaTest {

    @Mock
    private SecretsManagerUtil secretsManagerUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleRequest() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        EventCanaryLambda eventCanaryLambda = spy(new EventCanaryLambda(secretsManagerUtil, realMapper));
        doReturn("mock-token").when(eventCanaryLambda).createAccessToken();

        GitHub mockGitHub = mock(GitHub.class);
        GHRepository mockRepo = mock(GHRepository.class);
        GHLabel mockLabel = mock(GHLabel.class);

        try (MockedConstruction<GitHubBuilder> gitHubBuilder = mockConstruction(
                GitHubBuilder.class,
                (builderMock, context) -> {
                    when(builderMock.withOAuthToken(any())).thenReturn(builderMock);
                    when(builderMock.build()).thenReturn(mockGitHub);
                })) {
            when(mockGitHub.getRepository(any())).thenReturn(mockRepo);
            when(mockRepo.getLabel(anyString())).thenThrow(new GHFileNotFoundException());
            when(mockRepo.createLabel(anyString(), anyString(), anyString())).thenReturn(mockLabel);

            eventCanaryLambda.handleRequest(null, mock(Context.class));

            verify(eventCanaryLambda).createAccessToken();
            verify(mockRepo).createLabel(
                    eq("s3-data-lake-app-canary-label"),
                    eq("0366d6"),
                    eq("Canary label to test s3 data lake app")
            );
            verify(mockLabel).delete();
        }
    }

    @Test
    public void testHandleRequestLabelExists() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        EventCanaryLambda eventCanaryLambda = spy(new EventCanaryLambda(secretsManagerUtil, realMapper));
        doReturn("mock-token").when(eventCanaryLambda).createAccessToken();

        GitHub mockGitHub = mock(GitHub.class);
        GHRepository mockRepo = mock(GHRepository.class);
        GHLabel mockLabel = mock(GHLabel.class);

        try (MockedConstruction<GitHubBuilder> gitHubBuilder = mockConstruction(
                GitHubBuilder.class,
                (builderMock, context) -> {
                    when(builderMock.withOAuthToken(any())).thenReturn(builderMock);
                    when(builderMock.build()).thenReturn(mockGitHub);
                })) {
            when(mockGitHub.getRepository(any())).thenReturn(mockRepo);
            when(mockRepo.getLabel(anyString())).thenReturn(mockLabel);

            eventCanaryLambda.handleRequest(null, mock(Context.class));

            verify(eventCanaryLambda).createAccessToken();
            verify(mockLabel).delete();
        }
    }

    @Test
    public void testHandleRequestOtherException() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        EventCanaryLambda eventCanaryLambda = spy(new EventCanaryLambda(secretsManagerUtil, realMapper));
        doReturn("mock-token").when(eventCanaryLambda).createAccessToken();

        GitHub mockGitHub = mock(GitHub.class);
        GHRepository mockRepo = mock(GHRepository.class);

        try (MockedConstruction<GitHubBuilder> gitHubBuilder = mockConstruction(
                GitHubBuilder.class,
                (builderMock, context) -> {
                    when(builderMock.withOAuthToken(any())).thenReturn(builderMock);
                    when(builderMock.build()).thenReturn(mockGitHub);
                })) {
            when(mockGitHub.getRepository(any())).thenReturn(mockRepo);
            when(mockRepo.getLabel(anyString())).thenThrow(new RuntimeException());

            assertThrows(RuntimeException.class, () -> eventCanaryLambda.handleRequest(null, mock(Context.class)));
        }
    }
}
