/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseRepoFetcher {


    @Inject
    public ReleaseRepoFetcher() {
    }

    public Map<String, String> getReleaseRepos(String releaseVersion) {
        Map<String, String> repoMap = new HashMap<>();
        String[] urls = {
                String.format("https://raw.githubusercontent.com/opensearch-project/opensearch-build/main/manifests/%s/opensearch-%s.yml", releaseVersion, releaseVersion),
                String.format("https://raw.githubusercontent.com/opensearch-project/opensearch-build/main/manifests/%s/opensearch-dashboards-%s.yml", releaseVersion, releaseVersion)
        };
        for (String url : urls) {
            String responseBody = readUrl(url);
            parseYaml(responseBody, repoMap);
        }
        repoMap.putAll(releaseRepoExceptionMap());
        return repoMap;
    }


    public Map<String, String> releaseRepoExceptionMap() {
        Map<String, String> repoExceptionMap = new HashMap<>();
        repoExceptionMap.put("opensearch-build", "opensearch-build");
        repoExceptionMap.put("performance-analyzer-rca", "performance-analyzer-rca");
        repoExceptionMap.put("project-website", "project-website");
        repoExceptionMap.put("documentation-website", "documentation-website");
        return repoExceptionMap;
    };

    public String readUrl(String url) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(createURL(url).openStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    public URL createURL(String url){
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    public void parseYaml(String responseBody, Map<String, String> repoMap) {
        new Yaml().loadAll(responseBody).forEach(document -> {
            if (document instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) document;
                map.values().stream()
                        .filter(value -> value instanceof List)
                        .flatMap(value -> ((List<?>) value).stream())
                        .filter(component -> component instanceof Map)
                        .map(component -> (Map<?, ?>) component)
                        .forEach(componentMap -> {
                            String repoUrl = componentMap.get("repository").toString();
                            String componentName = componentMap.get("name").toString();

                            int startIndex = repoUrl.lastIndexOf('/') + 1;
                            int endIndex = repoUrl.lastIndexOf(".git");
                            String repoName = (endIndex != -1) ? repoUrl.substring(startIndex, endIndex) : repoUrl.substring(startIndex);

                            repoMap.put(componentName, repoName);
                        });
            }
        });
    }
}

