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
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GHFileNotFoundException;
import org.opensearchmetrics.dagger.DaggerServiceComponent;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.github.GhAppClient;
import org.opensearchmetrics.util.SecretsManagerUtil;

import java.io.IOException;

@Slf4j
public class EventCanaryLambda extends GhAppClient implements RequestHandler<Void, Void> {
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
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
        GHLabel label = null;
        GHRepository repository = null;
        try {
            String accessToken = createAccessToken();
            GitHub gitHub = new GitHubBuilder().withOAuthToken(accessToken).build();
            repository = gitHub.getRepository(GITHUB_REPO_TARGET);

            label = repository.getLabel(LABEL_NAME);
            System.out.println("Label already exists");
        } catch (GHFileNotFoundException e) {
            try {
                label = repository.createLabel(
                        LABEL_NAME,
                        "0366d6",  // color in hex
                        "Canary label to test s3 data lake app"
                );
                System.out.println("Label created successfully");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            System.out.println("Label canary FAILED");
            throw new RuntimeException("Failed to run label canary", e);
        }
        finally {
            if (label != null) {
                try {
                    label.delete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Label deleted successfully");
            }
        }
        return null;
    }
}
