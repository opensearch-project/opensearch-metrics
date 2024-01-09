package org.opensearchhealth.health.releasestats;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.health.model.Release;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseStats {

    private final OpenSearchUtil openSearchUtil;

    private final ObjectMapper objectMapper;

    public ReleaseStats(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper) {
        this.openSearchUtil = openSearchUtil;
        this.objectMapper = objectMapper;
    }


    public Release.ReleaseIssue getPluginReleaseIssue(String repository, String version) throws IOException {
        Release.ReleaseIssue buildReleaseIssue = null;
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repository));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", version));
        boolQueryBuilder.must(QueryBuilders.prefixQuery("title.keyword", "[RELEASE] Release version"));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"number", "html_url", "title"}, null);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                buildReleaseIssue = objectMapper.readValue(hitJson, Release.ReleaseIssue.class);
            }
        }
        return buildReleaseIssue;
    }

    public Map<String, String> getPluginReleaseIssues(String repository, String version) throws IOException {
        Map<String, String> pluginReleaseIssues = new HashMap<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repository));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", version));
        boolQueryBuilder.mustNot(QueryBuilders.prefixQuery("title.keyword", "[RELEASE] Release version"));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"html_url", "title"}, null);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                Release.ReleaseIssue pluginReleaseIssue = objectMapper.readValue(hitJson, Release.ReleaseIssue.class);
                pluginReleaseIssues.put(pluginReleaseIssue.getTitle(), pluginReleaseIssue.getHtmlUrl());
            }
        }
        return pluginReleaseIssues;
    }

    public List<Release.ReleaseIssue> getBuildReleaseIssues() throws IOException {
        List<Release.ReleaseIssue> buildReleaseIssue = new ArrayList<>();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", "opensearch-build"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
        boolQueryBuilder.must(QueryBuilders.prefixQuery("title.keyword", "[RELEASE] Release version"));
        SearchRequest searchRequest = new SearchRequest("github_issues");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"number", "html_url", "title"}, null);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                buildReleaseIssue.add(objectMapper.readValue(hitJson, Release.ReleaseIssue.class));
            }
        }
        return buildReleaseIssue;
    }


}
