package org.opensearchhealth.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramInterval;
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



    public void generate(List<String> repositories) throws Exception {

        Map<String, String> healthData = new HashMap<>();
        for (String repo : repositories) {
            Health row = new Health();
            row.setId(String.valueOf((UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE)));
            row.setCurrentDate(currentDate.toString());
            row.setRepository(repo);
            List<Health.Theme> themes = new ArrayList<>();
            List<String> actionItems = new ArrayList<>();
            for (ThemeFactors themeFactor : ThemeFactors.values()) {
                Health.Theme theme = new Health.Theme();
                theme.setName(themeFactor.getFullName());
                List<Health.Factor> factors = new ArrayList<>();
                for (HealthRequest healthRequest : themeFactor.getFactors(repo)) {
                    Health.Factor factor = new Health.Factor();
                    factor.setName(healthRequest.getFactor().getFullName());
                    BoolQueryBuilder countBoolQueryBuilder = healthRequest.getFactor().getBoolQueryBuilder(healthRequest, currentDate, currentDate);
                    SearchRequest countSearchRequest = healthRequest.getFactor().createSearchRequest(healthRequest, DateHistogramInterval.MONTH,  countBoolQueryBuilder, AggregationType.TOTAL_VALUE_COUNT);
                    long factorCount = healthRequest.getFactor().performSearch(countSearchRequest, healthRequest.getAggType(), AggregationType.TOTAL_VALUE_COUNT);
                    factor.setCount(factorCount);
                    if (healthRequest.getFactorThresholds() != null) {
                        BoolQueryBuilder thresholdBoolQueryBuilder = healthRequest.getFactorThresholds().getBoolQueryBuilder(healthRequest, currentDate, currentDate);
                        SearchRequest thresholdSearchRequest = healthRequest.getFactorThresholds().createSearchRequest(healthRequest, DateHistogramInterval.MONTH,  thresholdBoolQueryBuilder, AggregationType.TOTAL_VALUE_COUNT);
                        long thresholdCount = healthRequest.getFactorThresholds().performSearch(thresholdSearchRequest, healthRequest.getAggType(), AggregationType.TOTAL_VALUE_COUNT);
                        factor.setAllowedValue(thresholdCount);
                        if (factorCount > thresholdCount) {
                            factor.setThresholdStatus("red");
                            actionItems.add(healthRequest.getFactor().getFullName());
                        } else {
                            factor.setThresholdStatus("green");
                        }

                    } else {
                        factor.setAllowedValue(0);
                        factor.setThresholdStatus("NA");
                    }
                    factors.add(factor);
                }
                theme.setFactors(factors);
                themes.add(theme);
            }
            if (actionItems.isEmpty()) {
                actionItems.add("No Immediate Action Items");
            }
            row.setActionItems(actionItems);
            row.setThemes(themes);
            healthData.put(row.getId(), getJson(row));
        }
        opensearchUtil.createIndexIfNotExists("opensearch_health");
        opensearchUtil.bulkIndex("opensearch_health", healthData);
        System.out.println("The healthData is" + healthData);
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

