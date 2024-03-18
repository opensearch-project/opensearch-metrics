package org.opensearchmetrics.metrics.release;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReleaseNotesChecker extends UrlResponse {

    private final UrlResponse urlResponse;
    @Inject
    public ReleaseNotesChecker(UrlResponse urlResponse) {
        this.urlResponse = urlResponse;
    }

    public Boolean releaseNotes (String releaseVersion, String repo) {
        Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(releaseVersion);
        String releaseBranch = matcher.find() ? matcher.group(1) + "." + matcher.group(2) : "";
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
