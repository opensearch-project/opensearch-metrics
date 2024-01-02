package org.opensearchhealth.health;

import org.opensearchhealth.health.model.HealthRequest;
import org.opensearchhealth.health.projectcommunityhealth.ProjectCommunityFactors;

import java.util.List;
import java.util.Locale;

public enum ProjectThemeFactors {
    PROJECT_HEALTH("Project Health", "OpenSearch Project community metrics");

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

    public List<HealthRequest> getFactors() {
        switch (this) {
            case PROJECT_HEALTH:
                return List.of(
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.NUMBER_OF_CONTRIBUTORS, null, "github_contributors", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_OPEN_ISSUES, null, "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_CLOSED_ISSUES, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.GITHUB_OPEN_PULLS, null,  "github_pulls", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_PULLS_MERGED, null,  "github_pulls", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_ISSUES_CREATED, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_GITHUB_PULLS_CREATED, null,  "github_pulls", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_POSITIVE_REACTIONS_ISSUES, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.TOTAL_NEGATIVE_REACTIONS_ISSUES, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.ISSUES_AVG_TIME_OPEN, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.ISSUES_AVG_TIME_CLOSE, null,  "github_issues", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.PRS_AVG_TIME_OPEN, null,  "github_pulls", "opensearch-project"),
                        new HealthRequest(String.valueOf(this).toLowerCase(Locale.ROOT), ProjectCommunityFactors.PRS_AVG_TIME_CLOSE, null,  "github_pulls", "opensearch-project")
                );
            default:
                throw new RuntimeException("Unknown Project Theme");
        }
    }
}
