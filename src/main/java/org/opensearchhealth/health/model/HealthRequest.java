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

    public HealthRequest(String theme, Factors factor, Factors factorThresholds, String index, String repository) {
        this.theme = theme;
        this.factor = factor;
        this.factorThresholds = factorThresholds;
        this.index = index;
        this.repository = repository;
    }
}
