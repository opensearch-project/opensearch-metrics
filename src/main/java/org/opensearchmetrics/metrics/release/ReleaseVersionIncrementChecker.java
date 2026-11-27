/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.metrics.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReleaseVersionIncrementChecker {

    @Inject
    public ReleaseVersionIncrementChecker() {}


    public boolean releaseVersionIncrement(String releaseVersion, String repo, String branch, ObjectMapper objectMapper, OpenSearchUtil openSearchUtil) {
        if (repo.equals("OpenSearch")) {
            return checkOpenSearchVersion(releaseVersion, branch);
        } else if (repo.equals("OpenSearch-Dashboards") || repo.equals("opensearch-dashboards-functional-test")) {
            return checkOpenSearchDashboardsVersion(releaseVersion, repo, branch, objectMapper);
        } else {
            return checkGithubPulls(repo, releaseVersion, objectMapper, openSearchUtil);
        }
    }


    public boolean checkOpenSearchVersion(String releaseVersion, String branch) {
        String baseUrl = String.format("https://raw.githubusercontent.com/opensearch-project/OpenSearch/%s/", branch);
        return checkOpenSearchVersionAtUrl(releaseVersion, branch, baseUrl);
    }

    boolean checkOpenSearchVersionAtUrl(String releaseVersion, String branch, String baseUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + "gradle/libs.versions.toml").openConnection();
            try {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    System.err.println("Version catalog not found on " + branch + "; checking legacy version.properties");
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(new URL(baseUrl + "buildSrc/version.properties").openStream(), StandardCharsets.UTF_8))) {
                        Properties properties = new Properties();
                        properties.load(reader);
                        String version = properties.getProperty("opensearch");
                        if (version == null) {
                            throw new IOException("OpenSearch version missing from legacy version.properties");
                        }
                        Matcher matcher = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)").matcher(version);
                        if (matcher.find() == false) {
                            throw new IOException("Invalid OpenSearch version in legacy version.properties");
                        }
                        return matcher.group(1).equals(releaseVersion);
                    }
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    return readCatalogVersion(reader).equals(releaseVersion);
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static String readCatalogVersion(BufferedReader reader) throws IOException {
        Pattern section = Pattern.compile("^\\s*\\[([^]]+)\\]\\s*(?:#.*)?$");
        Pattern version = Pattern.compile("^\\s*opensearch\\s*=\\s*([\"'])(\\d+\\.\\d+\\.\\d+)\\1\\s*(?:#.*)?$");
        boolean inVersions = false;
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher sectionMatcher = section.matcher(line);
            if (sectionMatcher.matches()) {
                inVersions = sectionMatcher.group(1).equals("versions");
            } else if (inVersions) {
                Matcher versionMatcher = version.matcher(line);
                if (versionMatcher.matches()) {
                    return versionMatcher.group(2);
                }
            }
        }
        throw new IOException("OpenSearch version missing or invalid in gradle/libs.versions.toml");
    }

    public boolean checkOpenSearchDashboardsVersion(String releaseVersion, String repo, String branch, ObjectMapper objectMapper) {
        String url = String.format("https://raw.githubusercontent.com/opensearch-project/%s/%s/package.json", repo, branch);
        try {
            String content = new BufferedReader(new InputStreamReader(new URL(url).openStream()))
                    .lines()
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                    .toString();
            return objectMapper.readTree(content).get("version").asText().equals(releaseVersion);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkGithubPulls(String repo, String releaseVersion, ObjectMapper objectMapper, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("merged", true));
        boolQueryBuilder.must(QueryBuilders.matchQuery("pull_labels.keyword", "v" + releaseVersion));
        boolQueryBuilder.must(QueryBuilders.prefixQuery("title.keyword", "[AUTO] Increment version to"));
        SearchRequest searchRequest = new SearchRequest("github_pulls");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.fetchSource(new String[]{"merged"}, null);
        searchSourceBuilder.size(9000);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse;
        searchResponse = openSearchUtil.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        return Arrays.stream(searchHits)
                .map(SearchHit::getSourceAsString)
                .map(hitJson -> {
                    try {
                        return objectMapper.readValue(hitJson, Map.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .anyMatch(sourceMap -> sourceMap.get("merged") instanceof Boolean && (Boolean) sourceMap.get("merged"));
    }
}
