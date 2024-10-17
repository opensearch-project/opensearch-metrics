package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.dagger.DaggerServiceComponent;
import org.opensearchmetrics.dagger.ServiceComponent;
import org.opensearchmetrics.metrics.MetricsCalculation;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MetricsLambda extends AbstractBaseLambda {
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    private final OpenSearchUtil openSearchUtil;

    private final MetricsCalculation metricsCalculation;

    public MetricsLambda() {

        this(COMPONENT.getOpenSearchUtil(), COMPONENT.getMetricsCalculation());
    }

    @VisibleForTesting
    MetricsLambda(@NonNull OpenSearchUtil openSearchUtil, @NonNull MetricsCalculation metricsCalculation) {

        this.openSearchUtil = openSearchUtil;
        this.metricsCalculation = metricsCalculation;
    }

    @Override
    public Void handleRequest(Void input, Context context) {
        SearchRequest searchRequest = new SearchRequest("github_repos");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("repos")
                .field("repository.keyword").size(500);
        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        searchResponse = openSearchUtil.search(searchRequest);
        ParsedStringTerms termsAggregation = searchResponse.getAggregations().get("repos");
        List<String> keys = termsAggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        try {
            metricsCalculation.generateGeneralMetrics(keys);
            metricsCalculation.generateLabelMetrics(keys);
            metricsCalculation.generateReleaseMetrics();
            metricsCalculation.generateMaintainerMetrics(keys);
        } catch (Exception e) {
            throw new RuntimeException("Error running Metrics Calculation", e);
        }
        return input;
    }
}

