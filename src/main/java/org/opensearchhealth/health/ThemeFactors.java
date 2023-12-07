package org.opensearchhealth.health;

import org.opensearchhealth.health.communityhealth.CommunityFactors;
import org.opensearchhealth.health.githubhealth.GitHubFactorThresholds;
import org.opensearchhealth.health.githubhealth.GitHubFactors;
import org.opensearchhealth.health.model.HealthRequest;

import java.util.List;
import java.util.Locale;

/*
* Class to list down the Factors for a given Theme.
* */
public enum ThemeFactors {

    GITHUB_HEALTH("GitHub Health"),
    COMMUNITY_HEALTH("Community Health");

    private final String fullName;
    ThemeFactors(String fullName) {
        this.fullName = fullName;
    }


    public String getFullName() {
        return fullName;
    }

    public List<HealthRequest> getFactors(String repository) {
        switch (this) {
            case GITHUB_HEALTH:
                return List.of(
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.GITHUB_AUDIT, GitHubFactorThresholds.GITHUB_AUDIT, "github_audit", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES, GitHubFactorThresholds.UNTRIAGED_ISSUES, "github_issues", repository,"created_at", "untriaged_issues", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS, GitHubFactorThresholds.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS,  "github_issues", repository,"created_at", "untriaged_issues_30_days", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.ISSUES_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.ISSUES_NOT_RESPONDED_THIRTY_DAYS,  "github_issues", repository,"created_at", "issues_30_days", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.PRS_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.PRS_NOT_RESPONDED_THIRTY_DAYS,  "github_pulls", repository,"created_at", "pulls_30_days", HealthRequest.AggType.COUNT)
                );
            case COMMUNITY_HEALTH:
                return List.of(
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_GITHUB_ISSUES_CREATED, null, "github_issues", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_GITHUB_PULLS_CREATED, null, "github_pulls", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_GITHUB_PULLS_MERGED, null, "github_pulls", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.GITHUB_OPEN_ISSUES, null, "github_issues", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.GITHUB_OPEN_PULLS, null, "github_pulls", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_FORKS, null, "github_repos", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_STARS, null, "github_repos", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAl_SUBSCRIBERS, null, "github_repos", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), CommunityFactors.TOTAL_SIZE, null, "github_repos", repository,"current_date", "audit_status", HealthRequest.AggType.COUNT)
                );

            default:
                throw new RuntimeException("Unknown Theme");
        }
    }
}
