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
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.opensearchmetrics.dagger.DaggerServiceComponent;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.util.SecretsManagerUtil;
import org.opensearchmetrics.github.GhAppClient;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

@Slf4j
public class EventCanaryLambda extends GhAppClient implements RequestHandler<Void, Void> {
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    private final String GITHUB_OWNER_TARGET = System.getenv("GITHUB_OWNER_TARGET");
    private final String GITHUB_REPO_TARGET = System.getenv("GITHUB_REPO_TARGET");
    private final String LABEL_NAME = "s3-data-lake-app-canary-label";

    public EventCanaryLambda() {
        this(COMPONENT.getSecretsManagerUtil(), COMPONENT.getObjectMapper());
    }

    @VisibleForTesting
    EventCanaryLambda(@NonNull SecretsManagerUtil secretsManagerUtil, @NonNull ObjectMapper mapper) {
        super(secretsManagerUtil, mapper);
    }

    @Override
    public Void handleRequest(Void input, Context context) {
        try {
            String accessToken = createAccessToken();
            GitHub gitHub = new GitHubBuilder().withOAuthToken(accessToken).build();
            GHRepository repository = gitHub.getRepository(GITHUB_OWNER_TARGET + "/" + GITHUB_REPO_TARGET);
            GHLabel label = repository.createLabel(
                    LABEL_NAME,
                    "0366d6",
                    "Canary label to test s3 data lake app"
            );
            System.out.println("Label created successfully");
            label.delete();
            System.out.println("Label deleted successfully");
        } catch (Exception e) {
            System.out.println("Label canary FAILED");
            throw new RuntimeException("Failed to run label canary", e);
        }
        return null;
    }
}
