package org.opensearchmetrics.metrics.general;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

public interface Metrics {

    BoolQueryBuilder getBoolQueryBuilder(String repo);

    String searchIndex();

    default SearchRequest createSearchRequest(BoolQueryBuilder queryBuilder, String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(9000);
        searchRequest.source(searchSourceBuilder);
        return  searchRequest;
    };

    default long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request) {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            return searchResponse.getHits().getTotalHits().value;
        } else {
            throw new RuntimeException("Error connecting to the cluster");
        }
    }
}
