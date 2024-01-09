package org.opensearchhealth.lambda;

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
import org.opensearchhealth.dagger.DaggerServiceComponent;
import org.opensearchhealth.dagger.ServiceComponent;
import org.opensearchhealth.health.HealthCalculation;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HealthLambda extends AbstractBaseLambda {
    private static final ServiceComponent COMPONENT = DaggerServiceComponent.create();
    private final OpenSearchUtil openSearchUtil;

    private final HealthCalculation healthCalculation;

    public HealthLambda() {

        this(COMPONENT.getOpenSearchUtil(), COMPONENT.getGitHubHealthCalculation());
    }

    @VisibleForTesting
    HealthLambda(@NonNull OpenSearchUtil openSearchUtil, @NonNull HealthCalculation healthCalculation) {

        this.openSearchUtil = openSearchUtil;
        this.healthCalculation = healthCalculation;
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
        try {
            searchResponse = openSearchUtil.search(searchRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ParsedStringTerms termsAggregation = searchResponse.getAggregations().get("repos");
        List<String> keys = termsAggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        try {
            healthCalculation.generateRepos(keys);
            healthCalculation.generateProject();
            healthCalculation.generateReleaseStats(keys);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

