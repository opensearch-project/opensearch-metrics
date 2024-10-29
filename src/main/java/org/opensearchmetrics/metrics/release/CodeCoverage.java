/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.metrics.release;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearchmetrics.model.codecov.CodeCovResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Optional;

public class CodeCoverage {
    private final CloseableHttpClient httpClient;

    @Inject
    public CodeCoverage() {
        this(HttpClients.createDefault());
    }

    @VisibleForTesting
    CodeCoverage(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CodeCovResponse coverage(String branch, String repo) {
        String codeCovRepoURL = String.format("https://api.codecov.io/api/v2/github/opensearch-project/repos/%s/commits?branch=%s", repo, branch);
        CodeCovResponse codeCovResponse = new CodeCovResponse();
        codeCovResponse.setUrl(codeCovRepoURL);
        HttpGet request = new HttpGet(codeCovRepoURL);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (response.getStatusLine().getStatusCode() == 200) {
                String codeCovApiResult;
                try {
                    codeCovApiResult = EntityUtils.toString(response.getEntity());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                JSONObject jsonObject = new JSONObject(codeCovApiResult);
                JSONArray resultsCoverage = jsonObject.getJSONArray("results");
                Optional<JSONObject> firstResult = Optional.ofNullable(resultsCoverage)
                        .filter(results -> results.length() > 0)
                        .map(results -> results.getJSONObject(0));
                firstResult.ifPresentOrElse(
                        result -> {
                            codeCovResponse.setState(Optional.ofNullable(result.optString("state"))
                                    .orElse("no-coverage"));
                            codeCovResponse.setCommitId(result.optString("commitid", "none"));
                            codeCovResponse.setCoverage(
                                    Optional.ofNullable(result.optJSONObject("totals"))
                                            .map(totals -> totals.optDouble("coverage", 0.0))
                                            .orElse(0.0)
                            );
                        },
                        () -> {
                            codeCovResponse.setState("no-coverage");
                            codeCovResponse.setCommitId("none");
                            codeCovResponse.setCoverage(0.0);
                        }
                );
            }
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return codeCovResponse;
    }
}
