/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.opensearchmetrics.datasource.DataSourceType;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

public class SecretsManagerUtilTest {

    @Mock
    private AWSSecretsManager secretsManager;

    @Mock
    private ObjectMapper mapper;

    private SecretsManagerUtil secretsManagerUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        secretsManagerUtil = new SecretsManagerUtil(secretsManager, mapper);
    }

    @Test
    void testGetSlackCredentials() throws IOException {

        String slackWebhookURL = "slack-webhook-url";
        String slackChannel = "slack-channel";
        String slackUsername = "slack-username";
        SecretsManagerUtil.SlackCredentials slackCredentials = new SecretsManagerUtil.SlackCredentials();
        slackCredentials.setSlackWebhookURL(slackWebhookURL);
        slackCredentials.setSlackChannel(slackChannel);
        slackCredentials.setSlackUsername(slackUsername);

        String secretString = "secret-string-with-slack-credentials";
        GetSecretValueResult getSecretValueResult = new GetSecretValueResult();
        getSecretValueResult.setSecretString(secretString);

        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(getSecretValueResult);
        when(mapper.readValue(eq(secretString), eq(SecretsManagerUtil.SlackCredentials.class)))
                .thenReturn(slackCredentials);

        Optional<String> webhookURLResult = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_WEBHOOK_URL);
        Optional<String> channelResult = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_CHANNEL);
        Optional<String> usernameResult = secretsManagerUtil.getSlackCredentials(DataSourceType.SLACK_USERNAME);

        assertTrue(webhookURLResult.isPresent());
        assertTrue(channelResult.isPresent());
        assertTrue(usernameResult.isPresent());
        assertEquals(slackWebhookURL, webhookURLResult.get());
        assertEquals(slackChannel, channelResult.get());
        assertEquals(slackUsername, usernameResult.get());
    }

    @Test
    void testGetGitHubApiCredentials() throws IOException {
        String githubAppKey = "github-app-key";
        String githubAppId = "github-app-id";
        String githubAppInstallId = "github-app-install-id";
        SecretsManagerUtil.GitHubAppCredentials githubAppCredentials = new SecretsManagerUtil.GitHubAppCredentials();
        githubAppCredentials.setGithubAppKey(githubAppKey);
        githubAppCredentials.setGithubAppId(githubAppId);
        githubAppCredentials.setGithubAppInstallId(githubAppInstallId);

        String secretString = "secret-string-with-github-credentials";
        GetSecretValueResult getSecretValueResult = new GetSecretValueResult();
        getSecretValueResult.setSecretString(secretString);

        when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(getSecretValueResult);
        when(mapper.readValue(eq(secretString), eq(SecretsManagerUtil.GitHubAppCredentials.class)))
                .thenReturn(githubAppCredentials);

        Optional<String> appKeyResult = secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_KEY);
        Optional<String> appIdResult = secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_ID);
        Optional<String> appInstallIdResult = secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_INSTALL_ID);

        assertTrue(appKeyResult.isPresent());
        assertTrue(appIdResult.isPresent());
        assertTrue(appInstallIdResult.isPresent());
        assertEquals(githubAppKey, appKeyResult.get());
        assertEquals(githubAppId, appIdResult.get());
        assertEquals(githubAppInstallId, appInstallIdResult.get());
    }
}
