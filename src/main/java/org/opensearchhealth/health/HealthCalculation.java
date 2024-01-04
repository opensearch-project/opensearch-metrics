package org.opensearchhealth.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchhealth.health.model.Health;
import org.opensearchhealth.health.model.HealthRequest;
import org.opensearchhealth.util.OpenSearchUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class HealthCalculation {

    private final LocalDateTime currentDate;
    private final OpenSearchUtil opensearchUtil;
    private final ObjectMapper objectMapper;


    public HealthCalculation(OpenSearchUtil opensearchUtil, ObjectMapper objectMapper) {
        this.currentDate = LocalDateTime.now(ZoneId.of("UTC"));
        this.opensearchUtil = opensearchUtil;
        this.objectMapper = objectMapper;
    }



    public void generateRepos(List<String> repositories) throws Exception {

        Map<String, String> healthData = new HashMap<>();
        for (String repo : repositories) {
            Health row = new Health();
            row.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
            row.setCurrentDate(currentDate.toString());
            row.setRepository(repo);
            List<Health.Theme> themes = new ArrayList<>();
            List<String> actionItems = new ArrayList<>();
            for (RepoThemeFactors themeFactor : RepoThemeFactors.values()) {
                Health.Theme theme = new Health.Theme();
                theme.setName(themeFactor.getFullName());
                theme.setThemeDescription(themeFactor.getDescription());
                List<Health.Factor> factors = new ArrayList<>();
                for (HealthRequest healthRequest : themeFactor.getFactors(repo)) {
                    Health.Factor factor = new Health.Factor();
                    factor.setName(healthRequest.getFactor().getFullName());
                    factor.setFactorDescription(healthRequest.getFactor().getDescription());
                    BoolQueryBuilder countBoolQueryBuilder = healthRequest.getFactor().getBoolQueryBuilder(healthRequest, currentDate, currentDate);
                    SearchRequest countSearchRequest = healthRequest.getFactor().createSearchRequest(healthRequest,  countBoolQueryBuilder);
                    long factorCount = healthRequest.getFactor().performSearch(opensearchUtil, countSearchRequest);
                    factor.setCount(factorCount);
                    factor.setFactorStringValue(healthRequest.getFactor().getFactorStringValue(factorCount));
                    if (healthRequest.getFactorThresholds() != null) {
                        BoolQueryBuilder thresholdBoolQueryBuilder = healthRequest.getFactorThresholds().getBoolQueryBuilder(healthRequest, currentDate, currentDate);
                        SearchRequest thresholdSearchRequest = healthRequest.getFactorThresholds().createSearchRequest(healthRequest,  thresholdBoolQueryBuilder);
                        long thresholdCount = healthRequest.getFactorThresholds().performSearch(opensearchUtil, thresholdSearchRequest);
                        factor.setAllowedValue(thresholdCount);
                        if (factorCount > thresholdCount) {
                            factor.setThresholdStatus("red");
                            actionItems.add(healthRequest.getFactor().getFullName());
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
            healthData.put(row.getId(), getJson(row));
        }
        opensearchUtil.createIndexIfNotExists("opensearch_repo_health");
        opensearchUtil.bulkIndex("opensearch_repo_health", healthData);
    }

    public void generateProject() throws Exception {
        Map<String, String> healthData = new HashMap<>();
        Health row = new Health();
        row.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
        row.setCurrentDate(currentDate.toString());
        row.setRepository("opensearch-project");
        List<Health.Theme> themes = new ArrayList<>();
        List<String> actionItems = new ArrayList<>();
        for (ProjectThemeFactors projectThemeFactor : ProjectThemeFactors.values()) {
            Health.Theme theme = new Health.Theme();
            theme.setName(projectThemeFactor.getFullName());
            theme.setThemeDescription(projectThemeFactor.getDescription());
            List<Health.Factor> factors = new ArrayList<>();
            for (HealthRequest healthRequest : projectThemeFactor.getFactors()) {
                Health.Factor factor = new Health.Factor();
                factor.setName(healthRequest.getFactor().getFullName());
                factor.setFactorDescription(healthRequest.getFactor().getDescription());
                BoolQueryBuilder countBoolQueryBuilder = healthRequest.getFactor().getBoolQueryBuilder(healthRequest, currentDate, currentDate);
                SearchRequest countSearchRequest = healthRequest.getFactor().createSearchRequest(healthRequest,  countBoolQueryBuilder);
                long factorCount = healthRequest.getFactor().performSearch(opensearchUtil, countSearchRequest);
                factor.setCount(factorCount);
                factors.add(factor);
            }
            theme.setFactors(factors);
            themes.add(theme);
        }
        row.setActionItems(actionItems);
        row.setThemes(themes);
        healthData.put(row.getId(), getJson(row));
        opensearchUtil.createIndexIfNotExists("opensearch_project_health");
        opensearchUtil.bulkIndex("opensearch_project_health", healthData);
    }


    private String getJson(Health row) {
        try {
            return row.toJson(objectMapper);
        } catch (JsonProcessingException e) {
            log.error("Error while serializing ReportDataRow to JSON", e);
            throw new RuntimeException(e);
        }
    }
}

