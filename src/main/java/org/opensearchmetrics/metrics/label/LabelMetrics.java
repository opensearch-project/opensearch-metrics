package org.opensearchmetrics.metrics.label;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelMetrics {

    @Inject
    public LabelMetrics() {}

    public Map<String, List<Long>> getLabelInfo(String repo, OpenSearchUtil openSearchUtil) throws IOException {
        Map<String, Long> labelIssues = getLabelIssues(repo, openSearchUtil);
        Map<String, Long> labelPulls = getLabelPulls(repo, openSearchUtil);
        Set<String> combinedKeys = new HashSet<>(labelIssues.keySet());
        combinedKeys.addAll(labelPulls.keySet());
        Map<String, List<Long>> labelInfo = combinedKeys.stream()
                .collect(
                        LinkedHashMap::new,
                        (map, key) -> {
                            Long labelIssuesValue = labelIssues.getOrDefault(key, 0L);
                            Long labelPullsValue = labelPulls.getOrDefault(key, 0L);
                            List<Long> labelCountValues = new ArrayList<>();
                            labelCountValues.add(labelIssuesValue);
                            labelCountValues.add(labelPullsValue);
                            map.put(key, labelCountValues);
                        },
                        HashMap::putAll
                );
        return labelInfo;
    }

    public Map<String, Long> getLabelIssues(String repo, OpenSearchUtil openSearchUtil) {
      Map<String, Long> labelIssues = new HashMap<>();
      BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
      boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
      boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
      boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
      SearchRequest searchRequest = new SearchRequest("github_issues");
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      searchSourceBuilder.query(boolQueryBuilder);
      searchSourceBuilder.size(0);
      searchSourceBuilder.aggregation(
        AggregationBuilders.terms("label_issues")
                .field("issue_labels.keyword")
                .size(100000)
      );
      searchRequest.source(searchSourceBuilder);
      SearchResponse searchResponse = openSearchUtil.search(searchRequest);
      RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            Terms repoAggregation = searchResponse.getAggregations().get("label_issues");
            for (Terms.Bucket bucket : repoAggregation.getBuckets()) {
                labelIssues.put(bucket.getKeyAsString(), bucket.getDocCount());
            }
        }
        return labelIssues;
    }


    public Map<String, Long> getLabelPulls(String repo, OpenSearchUtil openSearchUtil) throws IOException {
        Map<String, Long> pullIssues = new HashMap<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        SearchRequest searchRequest = new SearchRequest("github_pulls");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("pull_issues")
                        .field("pull_labels.keyword")
                        .size(100000)
        );
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            Terms repoAggregation = searchResponse.getAggregations().get("pull_issues");
            for (Terms.Bucket bucket : repoAggregation.getBuckets()) {
                pullIssues.put(bucket.getKeyAsString(), bucket.getDocCount());
            }
        }
        return pullIssues;
    }
}
