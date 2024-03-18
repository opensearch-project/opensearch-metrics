package org.opensearchmetrics.metrics.release;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReleaseBranchChecker {

    private final UrlResponse urlResponse;
    @Inject
    public ReleaseBranchChecker(UrlResponse urlResponse) {
        this.urlResponse = urlResponse;
    }

    public Boolean releaseBranch (String releaseVersion, String repo) {
        Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(releaseVersion);
        String releaseBranch = matcher.find() ? matcher.group(1) + "." + matcher.group(2) : "";
        String releaseBranchUrl = String.format("https://github.com/opensearch-project/%s/tree/%s", repo, releaseBranch);

        try {
            int responseCode = urlResponse.getUrlResponse(releaseBranchUrl).getResponseCode();
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
