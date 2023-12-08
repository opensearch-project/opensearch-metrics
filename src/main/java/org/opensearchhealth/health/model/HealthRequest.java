package org.opensearchhealth.health.model;

import lombok.Data;
import org.opensearchhealth.health.Factors;

@Data
public class HealthRequest {

    private final String theme;

    private final Factors factor;

    private final Factors factorThresholds;
    private final String index;
    private final String repository;
    private final String dateField;
    private final String aggField;
    private final AggType aggType;

    public HealthRequest(String theme, Factors factor, Factors factorThresholds, String index, String repository, String dateField,
                         String aggField, AggType aggType) {
        this.theme = theme;
        this.factor = factor;
        this.factorThresholds = factorThresholds;
        this.index = index;
        this.repository = repository;
        this.dateField = dateField;
        this.aggField = aggField;
        this.aggType = aggType;
    }

    public enum AggType {
        COUNT, AVG, MAX
    }
}
