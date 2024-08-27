package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class ReleaseMetrics {

    private final OpenSearchUtil openSearchUtil;
    private final ObjectMapper objectMapper;
    private final ReleaseRepoFetcher releaseRepoFetcher;

    private final ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher;
    private final ReleaseLabelPullsFetcher releaseLabelPullsFetcher;

    private final ReleaseVersionIncrementChecker releaseVersionIncrementChecker;

    private final ReleaseBranchChecker releaseBranchChecker;

    private final ReleaseNotesChecker releaseNotesChecker;

    private final ReleaseIssueChecker releaseIssueChecker;

    @Inject
    public ReleaseMetrics(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper, ReleaseRepoFetcher releaseRepoFetcher,
                          ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher, ReleaseLabelPullsFetcher releaseLabelPullsFetcher,
                          ReleaseVersionIncrementChecker releaseVersionIncrementChecker, ReleaseBranchChecker releaseBranchChecker,
                          ReleaseNotesChecker releaseNotesChecker, ReleaseIssueChecker releaseIssueChecker) {
        this.openSearchUtil = openSearchUtil;
        this.objectMapper = objectMapper;
        this.releaseRepoFetcher = releaseRepoFetcher;
        this.releaseLabelIssuesFetcher = releaseLabelIssuesFetcher;
        this.releaseLabelPullsFetcher = releaseLabelPullsFetcher;
        this.releaseVersionIncrementChecker = releaseVersionIncrementChecker;
        this.releaseBranchChecker = releaseBranchChecker;
        this.releaseNotesChecker = releaseNotesChecker;
        this.releaseIssueChecker = releaseIssueChecker;
    }

    public Map<String, String> getReleaseRepos(String releaseVersion) {
        return releaseRepoFetcher.getReleaseRepos(releaseVersion);
    }


    public Long getReleaseLabelIssues(String releaseVersion, String repo, String issueState, boolean autoCut) {
        return releaseLabelIssuesFetcher.releaseLabelIssues(releaseVersion, repo, issueState, autoCut, openSearchUtil);
    }

    public Long getReleaseLabelPulls(String releaseVersion, String repo, String pullState) {
        return releaseLabelPullsFetcher.releaseLabelPulls(releaseVersion, repo, pullState, openSearchUtil);
    }

    public boolean getReleaseVersionIncrement (String releaseVersion, String repo, String branch) {
        return releaseVersionIncrementChecker.releaseVersionIncrement(releaseVersion, repo, branch, objectMapper, openSearchUtil);
    }

    public Boolean getReleaseNotes (String releaseVersion, String repo, String releaseBranch) {
        return releaseNotesChecker.releaseNotes(releaseVersion, repo, releaseBranch);
    }

    public Boolean getReleaseBranch (String releaseVersion, String repo) {
        return releaseBranchChecker.releaseBranch(releaseVersion, repo);
    }

    public String[] getReleaseOwners (String releaseVersion, String repo) {
        return releaseIssueChecker.releaseOwners(releaseVersion, repo, openSearchUtil);
    }

    public String getReleaseIssue (String releaseVersion, String repo) {
        return releaseIssueChecker.releaseIssue(releaseVersion, repo, openSearchUtil);
    }


}
