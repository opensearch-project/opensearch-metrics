/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.util;

import org.opensearchmetrics.datasource.DataSourceType;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class SecretsManagerUtil {
    private static final String API_CREDENTIALS_SECRETS = "API_CREDENTIALS_SECRETS";
    private final AWSSecretsManager secretsManager;
    private final ObjectMapper mapper;

    public SecretsManagerUtil(AWSSecretsManager secretsManager, ObjectMapper mapper) {
        this.secretsManager = secretsManager;
        this.mapper = mapper;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class SlackCredentials {
        @JsonProperty("slackWebhookURL")
        private String slackWebhookURL;
        @JsonProperty("slackChannel")
        private String slackChannel;
        @JsonProperty("slackUsername")
        private String slackUsername;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class GitHubAppCredentials {
        @JsonProperty("githubAppKey")
        private String githubAppKey;
        @JsonProperty("githubAppId")
        private String githubAppId;
        @JsonProperty("githubAppInstallId")
        private String githubAppInstallId;
    }

    public Optional<String> getSlackCredentials(DataSourceType datasourceType) throws IOException {
        String secretName = System.getenv(API_CREDENTIALS_SECRETS);
        log.info("Retrieving secrets value from secrets = {} ", secretName);
        GetSecretValueResult getSecretValueResult =
                secretsManager.getSecretValue(new GetSecretValueRequest().withSecretId(secretName));
        log.info("Successfully retrieved secrets for data source credentials");
        SlackCredentials credentials =
                mapper.readValue(getSecretValueResult.getSecretString(), SlackCredentials.class);
        switch (datasourceType) {
            case SLACK_WEBHOOK_URL:
                return Optional.of(credentials.getSlackWebhookURL());
            case SLACK_CHANNEL:
                return Optional.of(credentials.getSlackChannel());
            case SLACK_USERNAME:
                return Optional.of(credentials.getSlackUsername());
            default:
                return Optional.empty();
        }
    }

    public Optional<String> getGitHubAppCredentials(DataSourceType datasourceType) throws IOException {
        String secretName = System.getenv(API_CREDENTIALS_SECRETS);
        log.info("Retrieving secrets value from secrets = {} ", secretName);
        GetSecretValueResult getSecretValueResult =
                secretsManager.getSecretValue(new GetSecretValueRequest().withSecretId(secretName));
        log.info("Successfully retrieved secrets for data source credentials");
        GitHubAppCredentials credentials =
                mapper.readValue(getSecretValueResult.getSecretString(), GitHubAppCredentials.class);
        switch (datasourceType) {
            case GITHUB_APP_KEY:
                return Optional.of(credentials.getGithubAppKey());
            case GITHUB_APP_ID:
                return Optional.of(credentials.getGithubAppId());
            case GITHUB_APP_INSTALL_ID:
                return Optional.of(credentials.getGithubAppInstallId());
            default:
                return Optional.empty();
        }
    }
}
