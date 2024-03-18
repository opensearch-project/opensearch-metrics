package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.util.List;

public class ReleaseMetrics {

    private final OpenSearchUtil openSearchUtil;
    private final ObjectMapper objectMapper;
    private final ReleaseRepoFetcher releaseRepoFetcher;

    private final ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher;
    private final ReleaseLabelPullsFetcher releaseLabelPullsFetcher;

    private final ReleaseVersionIncrementChecker releaseVersionIncrementChecker;

    private final ReleaseBranchChecker releaseBranchChecker;

    private final ReleaseNotesChecker releaseNotesChecker;


    @Inject
    public ReleaseMetrics(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper, ReleaseRepoFetcher releaseRepoFetcher,
                          ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher, ReleaseLabelPullsFetcher releaseLabelPullsFetcher,
                          ReleaseVersionIncrementChecker releaseVersionIncrementChecker, ReleaseBranchChecker releaseBranchChecker,
                          ReleaseNotesChecker releaseNotesChecker) {
        this.openSearchUtil = openSearchUtil;
        this.objectMapper = objectMapper;
        this.releaseRepoFetcher = releaseRepoFetcher;
        this.releaseLabelIssuesFetcher = releaseLabelIssuesFetcher;
        this.releaseLabelPullsFetcher = releaseLabelPullsFetcher;
        this.releaseVersionIncrementChecker = releaseVersionIncrementChecker;
        this.releaseBranchChecker = releaseBranchChecker;
        this.releaseNotesChecker = releaseNotesChecker;
    }


    public List<String> getReleaseRepos(String releaseVersion) {
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


    public Boolean getReleaseNotes (String releaseVersion, String repo) {
        return releaseNotesChecker.releaseNotes(releaseVersion, repo);
    }



    public Boolean getReleaseBranch (String releaseVersion, String repo) {
        return releaseBranchChecker.releaseBranch(releaseVersion, repo);
    }


}
