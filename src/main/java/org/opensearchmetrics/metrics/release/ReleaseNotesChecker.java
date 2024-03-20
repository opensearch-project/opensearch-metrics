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
        if(repo.equals("OpenSearch")) {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch.release-notes-%s.md", repo, releaseBranch, releaseVersion);
        } else if (repo.equals("OpenSearch-Dashboards")) {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch-dashboards.release-notes-%s.md", repo, releaseBranch, releaseVersion);
        } else {
            releaseNotesUrl = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/release-notes/opensearch-%s.release-notes-%s.0.md", repo, releaseBranch, repo, releaseVersion);
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
