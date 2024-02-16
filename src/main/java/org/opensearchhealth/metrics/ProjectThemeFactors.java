package org.opensearchhealth.metrics;

import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.metrics.projectcommunitymetrics.ProjectCommunityFactors;
import org.opensearchhealth.metrics.projectgithubmetrics.ProjectGithubFactorThresholds;
import org.opensearchhealth.metrics.projectgithubmetrics.ProjectGithubFactors;

import java.util.List;
import java.util.Locale;

public enum ProjectThemeFactors {
    GITHUB_PROJECT_METRICS("Github Project Metrics", "OpenSearch Project GitHub metrics"),
    COMMUNITY_ENGAGEMENT("Community Engagement", "OpenSearch Project community metrics");

    private final String fullName;
    private final String description;
    ProjectThemeFactors(String fullName, String description) {
        this.fullName = fullName;
        this.description = description;
    }


    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

    public List<MetricsRequest> getFactors() {
        switch (this) {
            case GITHUB_PROJECT_METRICS:
                return List.of(
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.UNTRIAGED_ISSUES, ProjectGithubFactorThresholds.UNTRIAGED_ISSUES, "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.GITHUB_AUDIT, ProjectGithubFactorThresholds.GITHUB_AUDIT, "github_audit", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS, ProjectGithubFactorThresholds.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.ISSUES_NOT_RESPONDED_THIRTY_DAYS, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.PRS_NOT_RESPONDED, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectGithubFactors.PRS_NOT_RESPONDED_THIRTY_DAYS, null,  "github_pulls", "opensearch-project")
                );
            case COMMUNITY_ENGAGEMENT:
                return List.of(
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.ISSUES_AVG_TIME_OPEN, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.ISSUES_AVG_TIME_CLOSE, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.PRS_AVG_TIME_OPEN, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.PRS_AVG_TIME_MERGED, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.NUMBER_OF_CONTRIBUTORS, null, "github_contributors", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_OPEN_ISSUES, null, "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_CLOSED_ISSUES, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_OPEN_PULLS, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_PULLS_MERGED, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_ISSUES_CREATED, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_PULLS_CREATED, null,  "github_pulls", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_POSITIVE_REACTIONS_ISSUES, null,  "github_issues", "opensearch-project"),
                        new MetricsRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_NEGATIVE_REACTIONS_ISSUES, null,  "github_issues", "opensearch-project")
                );
            default:
                throw new RuntimeException("Unknown Project Theme");
        }
    }
}
