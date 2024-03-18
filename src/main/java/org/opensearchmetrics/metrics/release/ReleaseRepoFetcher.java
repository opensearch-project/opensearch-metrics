package org.opensearchmetrics.metrics.release;

import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReleaseRepoFetcher {


    @Inject
    public ReleaseRepoFetcher() {
    }

    public List<String> getReleaseRepos(String releaseVersion) {
        List<String> repoNames = new ArrayList<>();
        String[] urls = {
                String.format("https://raw.githubusercontent.com/opensearch-project/opensearch-build/main/manifests/%s/opensearch-%s.yml", releaseVersion, releaseVersion),
                String.format("https://raw.githubusercontent.com/opensearch-project/opensearch-build/main/manifests/%s/opensearch-dashboards-%s.yml", releaseVersion, releaseVersion)
        };
        for (String url : urls) {
            String responseBody = readUrl(url);
            parseYaml(responseBody, repoNames);
        }
        return repoNames.stream()
                .distinct()
                .collect(Collectors.toList());
    }

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


    public void parseYaml(String responseBody, List<String> repoNames) {
        new Yaml().loadAll(responseBody).forEach(document -> {
            if (document instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) document;
                map.values().stream()
                        .filter(value -> value instanceof List)
                        .flatMap(value -> ((List<?>) value).stream())
                        .filter(component -> component instanceof Map)
                        .map(component -> (Map<?, ?>) component)
                        .filter(componentMap -> {
                            Object repository = componentMap.get("repository");
                            return repository != null && repository.toString().contains("github.com");
                        })
                        .map(componentMap -> {
                            String repoUrl = componentMap.get("repository").toString();
                            int startIndex = repoUrl.lastIndexOf('/') + 1;
                            int endIndex = repoUrl.lastIndexOf(".git");
                            if (repoUrl != null && endIndex != -1) {
                                return repoUrl.substring(startIndex, endIndex);
                            } else {
                                return repoUrl.substring(startIndex);
                            }
                        })
                        .forEach(repoNames::add);
            }
        });
    }
}

