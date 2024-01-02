package org.opensearchhealth.health;

import org.opensearchhealth.health.repocommunityhealth.RepoCommunityFactors;
import org.opensearchhealth.health.githubhealth.GitHubFactorThresholds;
import org.opensearchhealth.health.githubhealth.GitHubFactors;
import org.opensearchhealth.health.model.HealthRequest;

import java.util.List;
import java.util.Locale;

/*
* Class to list down the Factors for a given Theme.
* */
public enum RepoThemeFactors {

    GITHUB_HEALTH("GitHub Health", "Repo overall GitHub data"),
    COMMUNITY_HEALTH("Community Health", "Repo overall community metrics");

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

    public List<HealthRequest> getFactors(String repository) {
        switch (this) {
            case GITHUB_HEALTH:
                return List.of(
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.GITHUB_AUDIT, GitHubFactorThresholds.GITHUB_AUDIT, "github_audit", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES, GitHubFactorThresholds.UNTRIAGED_ISSUES, "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS, GitHubFactorThresholds.UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS,  "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.ISSUES_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.ISSUES_NOT_RESPONDED_THIRTY_DAYS,  "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), GitHubFactors.PRS_NOT_RESPONDED_THIRTY_DAYS, GitHubFactorThresholds.PRS_NOT_RESPONDED_THIRTY_DAYS,  "github_pulls", repository)
                );
            case COMMUNITY_HEALTH:
                return List.of(
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_ISSUES_CREATED, null, "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_PULLS_CREATED, null, "github_pulls", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_GITHUB_PULLS_MERGED, null, "github_pulls", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_OPEN_ISSUES, null, "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_CLOSED_ISSUES, null, "github_issues", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.GITHUB_OPEN_PULLS, null, "github_pulls", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_FORKS, null, "github_repos", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_STARS, null, "github_repos", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAl_SUBSCRIBERS, null, "github_repos", repository),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), RepoCommunityFactors.TOTAL_SIZE, null, "github_repos", repository)
                );

            default:
                throw new RuntimeException("Unknown Repo Theme");
        }
    }
}
