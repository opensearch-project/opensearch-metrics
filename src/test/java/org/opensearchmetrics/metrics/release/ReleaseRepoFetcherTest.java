package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ReleaseRepoFetcherTest {

    @Test
    public void testReadUrl() throws IOException {
        String url = "https://example.com";
        String expectedContent = "Test content from URL\n";
        InputStream inputStream = new ByteArrayInputStream(expectedContent.getBytes());
        URL mockedUrl = Mockito.mock(URL.class);
        when(mockedUrl.openStream()).thenReturn(inputStream);
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        when(fetcher.createURL(url)).thenReturn(mockedUrl);
        String actualContent = fetcher.readUrl(url);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void testCreateURL() {
        String url = "https://example.com";
        ReleaseRepoFetcher fetcher = new ReleaseRepoFetcher();
        URL createdUrl = fetcher.createURL(url);
        assertEquals(url, createdUrl.toString());
    }

    @Test
    public void testParseYaml() {
        String responseBody = "---\n" +
                "schema-version: '1.1'\n" +
                "build:\n" +
                "  name: OpenSearch\n" +
                "  version: 1.3.15\n" +
                "ci:\n" +
                "  image:\n" +
                "    name: opensearchstaging/ci-runner:ci-runner-centos7-opensearch-build-v3\n" +
                "    args: -e JAVA_HOME=/opt/java/openjdk-11\n" +
                "components:\n" +
                "  - name: OpenSearch\n" +
                "    repository: https://github.com/opensearch-project/OpenSearch.git\n" +
                "    ref: tags/1.3.15\n" +
                "    checks:\n" +
                "      - gradle:publish\n" +
                "      - gradle:properties:version\n" +
                "  - name: common-utils\n" +
                "    repository: https://github.com/opensearch-project/common-utils.git\n" +
                "    ref: tags/1.3.15.0\n" +
                "    checks:\n" +
                "      - gradle:publish\n" +
                "      - gradle:properties:version\n" +
                "    platforms:\n" +
                "      - linux\n" +
                "      - windows\n";
        List<String> repoNames = new ArrayList<>();
        ReleaseRepoFetcher fetcher = new ReleaseRepoFetcher();
        fetcher.parseYaml(responseBody, repoNames);
        assertEquals(2, repoNames.size());
        assertEquals(new ArrayList<>(Arrays.asList("OpenSearch", "common-utils")), repoNames);
    }

    @Test
    public void testGetReleaseRepos() {
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        Mockito.doReturn("Test content").when(fetcher).readUrl(Mockito.anyString());
        List<String> repos = fetcher.getReleaseRepos("1.0.0");
        assertEquals(0, repos.size());
    }

    @Test
    public void testGetReleaseRepos_withData() {
        ReleaseRepoFetcher fetcher = Mockito.spy(new ReleaseRepoFetcher());
        Mockito.doReturn("Test content").when(fetcher).readUrl(Mockito.anyString());
        List<String> repoNames = new ArrayList<>();
        repoNames.add("repo1");
        Mockito.doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            List<String> list = (List<String>) args[1];
            list.addAll(repoNames);
            return null;
        }).when(fetcher).parseYaml(Mockito.anyString(), Mockito.anyList());
        List<String> repos = fetcher.getReleaseRepos("1.0.0");
        assertEquals(1, repos.size());
        assertEquals("repo1", repos.get(0));
    }

}