package org.opensearchhealth.metrics.model;

import lombok.Data;
import org.opensearchhealth.metrics.Factors;

@Data
public class MetricsRequest {

    private final String theme;

    private final Factors factor;

    private final Factors factorThresholds;
    private final String index;
    private final String repository;

    public MetricsRequest(String theme, Factors factor, Factors factorThresholds, String index, String repository) {
        this.theme = theme;
        this.factor = factor;
        this.factorThresholds = factorThresholds;
        this.index = index;
        this.repository = repository;
    }
}
