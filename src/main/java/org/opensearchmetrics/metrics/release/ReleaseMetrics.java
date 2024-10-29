/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
<<<<<<< HEAD

=======
>>>>>>> cf2adf2 (Add license checker and missing license headers)
package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearchmetrics.model.codecov.CodeCovResponse;
import org.opensearchmetrics.model.codecov.CodeCovResult;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.CoderResult;
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

    private final CodeCoverage codeCoverage;

    @Inject
    public ReleaseMetrics(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper, ReleaseRepoFetcher releaseRepoFetcher,
                          ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher, ReleaseLabelPullsFetcher releaseLabelPullsFetcher,
                          ReleaseVersionIncrementChecker releaseVersionIncrementChecker, ReleaseBranchChecker releaseBranchChecker,
                          ReleaseNotesChecker releaseNotesChecker, ReleaseIssueChecker releaseIssueChecker, CodeCoverage codeCoverage) {
        this.openSearchUtil = openSearchUtil;
        this.objectMapper = objectMapper;
        this.releaseRepoFetcher = releaseRepoFetcher;
        this.releaseLabelIssuesFetcher = releaseLabelIssuesFetcher;
        this.releaseLabelPullsFetcher = releaseLabelPullsFetcher;
        this.releaseVersionIncrementChecker = releaseVersionIncrementChecker;
        this.releaseBranchChecker = releaseBranchChecker;
        this.releaseNotesChecker = releaseNotesChecker;
        this.releaseIssueChecker = releaseIssueChecker;
        this.codeCoverage = codeCoverage;
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

    public CodeCovResponse getCodeCoverage (String branch, String repo) {
        return codeCoverage.coverage(branch, repo);
    }


}
