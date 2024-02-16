package org.opensearchhealth.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.metrics.model.Metrics;
import org.opensearchhealth.metrics.model.Release;
import org.opensearchhealth.metrics.releasestats.ReleaseStats;
import org.opensearchhealth.util.OpenSearchUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MetricsCalculation {

    private final LocalDateTime currentDate;
    private final OpenSearchUtil opensearchUtil;
    private final ObjectMapper objectMapper;


    public MetricsCalculation(OpenSearchUtil opensearchUtil, ObjectMapper objectMapper) {
        this.currentDate = LocalDateTime.now(ZoneId.of("UTC"));
        this.opensearchUtil = opensearchUtil;
        this.objectMapper = objectMapper;
    }



    public void generateRepos(List<String> repositories) throws Exception {

        Map<String, String> healthData = new HashMap<>();
        for (String repo : repositories) {
            Metrics row = new Metrics();
            row.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
            row.setCurrentDate(currentDate.toString());
            row.setRepository(repo);
            List<Metrics.Theme> themes = new ArrayList<>();
            List<String> actionItems = new ArrayList<>();
            for (RepoThemeFactors themeFactor : RepoThemeFactors.values()) {
                Metrics.Theme theme = new Metrics.Theme();
                theme.setName(themeFactor.getFullName());
                theme.setThemeDescription(themeFactor.getDescription());
                List<Metrics.Factor> factors = new ArrayList<>();
                for (MetricsRequest metricsRequest : themeFactor.getFactors(repo)) {
                    Metrics.Factor factor = new Metrics.Factor();
                    factor.setName(metricsRequest.getFactor().getFullName());
                    factor.setFactorDescription(metricsRequest.getFactor().getDescription());
                    BoolQueryBuilder countBoolQueryBuilder = metricsRequest.getFactor().getBoolQueryBuilder(metricsRequest, currentDate, currentDate);
                    SearchRequest countSearchRequest = metricsRequest.getFactor().createSearchRequest(metricsRequest,  countBoolQueryBuilder);
                    long factorCount = metricsRequest.getFactor().performSearch(opensearchUtil, countSearchRequest, objectMapper);
                    factor.setCount(factorCount);
                    factor.setFactorStringValue(metricsRequest.getFactor().getFactorStringValue(factorCount));
                    factor.setFactorMapValue(metricsRequest.getFactor().performSearchMapValue(opensearchUtil, countSearchRequest, objectMapper));
                    if (metricsRequest.getFactorThresholds() != null) {
                        BoolQueryBuilder thresholdBoolQueryBuilder = metricsRequest.getFactorThresholds().getBoolQueryBuilder(metricsRequest, currentDate, currentDate);
                        SearchRequest thresholdSearchRequest = metricsRequest.getFactorThresholds().createSearchRequest(metricsRequest,  thresholdBoolQueryBuilder);
                        long thresholdCount = metricsRequest.getFactorThresholds().performSearch(opensearchUtil, thresholdSearchRequest, objectMapper);
                        // factor.setAllowedValue(thresholdCount);
                        if (factorCount > thresholdCount) {
                            factor.setThresholdStatus("red");
                            actionItems.add(String.format("%s: %s", themeFactor.getFullName(), metricsRequest.getFactor().getFullName()));
                        } else {
                            factor.setThresholdStatus("green");
                        }

                    }
                    factors.add(factor);
                }
                theme.setFactors(factors);
                themes.add(theme);
            }
            row.setActionItems(actionItems);
            row.setThemes(themes);
            healthData.put(row.getId(), row.getJson(row, objectMapper));
        }
        opensearchUtil.createIndexIfNotExists("opensearch_repo_metrics");
        opensearchUtil.bulkIndex("opensearch_repo_metrics", healthData);
    }

    public void generateProject() throws Exception {
        Map<String, String> healthData = new HashMap<>();
        Metrics row = new Metrics();
        row.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
        row.setCurrentDate(currentDate.toString());
        row.setRepository("opensearch-project");
        List<Metrics.Theme> themes = new ArrayList<>();
        List<String> actionItems = new ArrayList<>();
        for (ProjectThemeFactors projectThemeFactor : ProjectThemeFactors.values()) {
            Metrics.Theme theme = new Metrics.Theme();
            theme.setName(projectThemeFactor.getFullName());
            theme.setThemeDescription(projectThemeFactor.getDescription());
            List<Metrics.Factor> factors = new ArrayList<>();
            for (MetricsRequest metricsRequest : projectThemeFactor.getFactors()) {
                Metrics.Factor factor = new Metrics.Factor();
                factor.setName(metricsRequest.getFactor().getFullName());
                factor.setFactorDescription(metricsRequest.getFactor().getDescription());
                BoolQueryBuilder countBoolQueryBuilder = metricsRequest.getFactor().getBoolQueryBuilder(metricsRequest, currentDate, currentDate);
                SearchRequest countSearchRequest = metricsRequest.getFactor().createSearchRequest(metricsRequest,  countBoolQueryBuilder);
                long factorCount = metricsRequest.getFactor().performSearch(opensearchUtil, countSearchRequest, objectMapper);
                factor.setCount(factorCount);
                factor.setFactorMapValue(metricsRequest.getFactor().performSearchMapValue(opensearchUtil, countSearchRequest, objectMapper));
                if (metricsRequest.getFactorThresholds() != null) {
                    BoolQueryBuilder thresholdBoolQueryBuilder = metricsRequest.getFactorThresholds().getBoolQueryBuilder(metricsRequest, currentDate, currentDate);
                    SearchRequest thresholdSearchRequest = metricsRequest.getFactorThresholds().createSearchRequest(metricsRequest,  thresholdBoolQueryBuilder);
                    long thresholdCount = metricsRequest.getFactorThresholds().performSearch(opensearchUtil, thresholdSearchRequest, objectMapper);
                    // factor.setAllowedValue(thresholdCount);
                    if (factorCount > thresholdCount) {
                        factor.setThresholdStatus("red");
                        actionItems.add(String.format("%s: %s", projectThemeFactor.getFullName(), metricsRequest.getFactor().getFullName()));
                    } else {
                        factor.setThresholdStatus("green");
                    }

                }
                factors.add(factor);
            }
            theme.setFactors(factors);
            themes.add(theme);
        }
        row.setActionItems(actionItems);
        row.setThemes(themes);
        healthData.put(row.getId(), row.getJson(row, objectMapper));
        opensearchUtil.createIndexIfNotExists("opensearch_project_metrics");
        opensearchUtil.bulkIndex("opensearch_project_metrics", healthData);
    }

    public void generateReleaseStats(List<String> repositories) throws Exception {
        ReleaseStats releaseStats = new ReleaseStats(opensearchUtil, objectMapper);
        List<Release.ReleaseIssue> buildReleaseIssues = releaseStats.getBuildReleaseIssues();
        for(Release.ReleaseIssue releaseIssue: buildReleaseIssues) {
            Map<String, String> releaseData = new HashMap<>();
            Release release = new Release();
            release.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
            release.setCurrentDate(currentDate.toString());
            release.setReleaseVersion(releaseIssue.getTitle().substring(releaseIssue.getTitle().lastIndexOf(' ') + 1));
            release.setReleaseIssueNumber(releaseIssue.getNumber());
            release.setReleaseIssueUrl(releaseIssue.getHtmlUrl());
            List<Release.Plugins> plugins = new ArrayList<>();
            for (String repo : repositories) {
                Release.Plugins plugin = new Release.Plugins();
                plugin.setName(repo);
                Release.ReleaseIssue pluginReleaseIssue = releaseStats.getPluginReleaseIssue(repo, "v"+releaseIssue.getTitle().substring(releaseIssue.getTitle().lastIndexOf(' ') + 1));
                if(pluginReleaseIssue != null) {
                    plugin.setPluginReleaseIssueNumber(pluginReleaseIssue.getNumber());
                    plugin.setPluginReleaseIssueUrl(pluginReleaseIssue.getHtmlUrl());
                }
                plugin.setPluginReleaseLabelOpenIssues(releaseStats.getPluginReleaseIssues(repo, "v"+releaseIssue.getTitle().substring(releaseIssue.getTitle().lastIndexOf(' ') + 1)));
                plugins.add(plugin);
            }
            release.setPlugins(plugins);
            releaseData.put(release.getId(), release.getJson(release, objectMapper));
            opensearchUtil.createIndexIfNotExists("opensearch_release_stats");
            opensearchUtil.bulkIndex("opensearch_release_stats", releaseData);
        }
    }
}

