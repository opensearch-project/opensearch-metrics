package org.opensearchhealth.metrics;

import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.metrics.repocommunitymetrics.RepoCommunityFactors;
import org.opensearchhealth.metrics.repogithubmetrics.GitHubFactorThresholds;
import org.opensearchhealth.metrics.repogithubmetrics.GitHubFactors;

import java.util.List;
import java.util.Locale;

/*
* Class to list down the Factors for a given Theme.
* */
public enum RepoThemeFactors {

    GITHUB_REPO_METRICS("GitHub Repo Metrics", "Repo overall GitHub data"),
    COMMUNITY_ENGAGEMENT("Community Engagement", "Repo overall community metrics");

    private final String fullName;
    private final String description;
    RepoThemeFactors(String fullName, String description) {
        this.fullName = fullName;
        this.description = description;
    }


    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

    public List<MetricsRequest> getFactors(String repository) {
        switch (this) {
            case GITHUB_REPO_METRICS:
                return List.of(
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.GITHUB_AUDIT, GitHubFactorThresholds.GITHUB_AUDIT, "github_audit", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES, GitHubFactorThresholds.UNTRIAGED_ISSUES, "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS, GitHubFactorThresholds.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.ISSUES_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.ISSUES_NOT_RESPONDED_THIRTY_DAYS,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.PRS_NOT_RESPONDED, GitHubFactorThresholds.PRS_NOT_RESPONDED,  "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.PRS_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.PRS_NOT_RESPONDED_THIRTY_DAYS,  "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.CODECOV_COVERAGE, null,  "codecov_coverage", repository)
                );
            case COMMUNITY_ENGAGEMENT:
                return List.of(
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_ISSUES_CREATED, null, "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_PULLS_CREATED, null, "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_PULLS_MERGED, null, "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_OPEN_ISSUES, null, "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_CLOSED_ISSUES, null, "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_OPEN_PULLS, null, "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_FORKS, null, "github_repos", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_STARS, null, "github_repos", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAl_SUBSCRIBERS, null, "github_repos", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_SIZE, null, "github_repos", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_POSITIVE_REACTIONS_ISSUES, null,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_NEGATIVE_REACTIONS_ISSUES, null,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.ISSUES_AVG_TIME_OPEN, null,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.ISSUES_AVG_TIME_CLOSE, null,  "github_issues", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.PRS_AVG_TIME_OPEN, null,  "github_pulls", repository),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.PRS_AVG_TIME_MERGED, null,  "github_pulls", repository)
                );

            default:
                throw new RuntimeException("Unknown Repo Theme");
        }
    }
}
