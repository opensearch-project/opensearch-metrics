/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;

public class ReleaseNotesChecker extends UrlResponse {

    private final UrlResponse urlResponse;
    @Inject
    public ReleaseNotesChecker(UrlResponse urlResponse) {
        this.urlResponse = urlResponse;
    }

    public Boolean releaseNotes(String releaseVersion, String repo, String releaseBranch) {
        String releaseNotesUrl;
        String[] versionSplit = releaseVersion.split("-");
        String qualifier = versionSplit.length > 1 ? versionSplit[1] : null;

        if(repo.equals("OpenSearch")) {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch.release-notes-%s.md", repo, releaseBranch, releaseVersion);
        } else if (repo.equals("OpenSearch-Dashboards")) {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch-dashboards.release-notes-%s.md", repo, releaseBranch, releaseVersion);
        } else if (qualifier == null){
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch-%s.release-notes-%s.0.md", repo, releaseBranch, repo, releaseVersion);
        } else {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch-%s.release-notes-%s.0-%s.md", repo, releaseBranch, repo, releaseVersion, qualifier);
        }
        try {
            int responseCode = urlResponse.getUrlResponse(releaseNotesUrl).getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
