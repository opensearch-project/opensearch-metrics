package org.opensearchmetrics.metrics.maintainer;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.BucketOrder;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.aggregations.metrics.TopHits;
import org.opensearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.opensearchmetrics.model.maintainer.EventData;
import org.opensearchmetrics.model.maintainer.MaintainerData;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MaintainerMetrics {

    @Inject
    public MaintainerMetrics() {
    }

    public List<String> getEventTypes(OpenSearchUtil openSearchUtil) {
        SearchRequest searchRequest = new SearchRequest("github_events");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("event_types")
                .field("type.keyword").size(500);
        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        ParsedStringTerms termsAggregation = searchResponse.getAggregations().get("event_types");
        List<String> eventTypes = termsAggregation.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        return eventTypes;
    }

    public Optional<EventData> queryLatestEvent(String repo, String userLogin, String eventType, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        boolQueryBuilder.must(QueryBuilders.matchQuery("actor.keyword", userLogin));
        boolQueryBuilder.must(QueryBuilders.matchQuery("type.keyword", eventType));
        SearchRequest searchRequest = new SearchRequest("github_events");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(0);
        TopHitsAggregationBuilder aggregation = AggregationBuilders.topHits("latest_event")
                .size(1).sort("created_at", SortOrder.DESC);
        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            TopHits topHits = searchResponse.getAggregations().get("latest_event");
            if (topHits.getHits().getHits().length > 0) {
                Map<String, Object> latestDocument = topHits.getHits().getHits()[0].getSourceAsMap();
                EventData eventData = new EventData();
                eventData.setEventAction(latestDocument.get("action").toString());
                eventData.setTimeLastEngaged(Instant.parse(latestDocument.get("created_at").toString()));
                return Optional.of(eventData);
            } else {
                return Optional.empty();
            }
        } else {
            throw new RuntimeException("Error connecting to the cluster");
        }

    }

    public long[] mostAndLeastRepoEventCounts(OpenSearchUtil openSearchUtil) {
        SearchRequest searchRequest = new SearchRequest("github_events");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder mostCommonTerms = AggregationBuilders
                .terms("most_event_count")
                .field("repository.keyword")
                .size(1)
                .order(BucketOrder.count(false));

        TermsAggregationBuilder leastCommonTerms = AggregationBuilders
                .terms("least_event_count")
                .field("repository.keyword")
                .size(1)
                .order(BucketOrder.count(true));

        searchSourceBuilder.aggregation(mostCommonTerms);
        searchSourceBuilder.aggregation(leastCommonTerms);
        searchSourceBuilder.size(0);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            Terms leastEventCount = searchResponse.getAggregations().get("least_event_count");
            Terms mostEventCount = searchResponse.getAggregations().get("most_event_count");
            for (Terms.Bucket leastBucket : leastEventCount.getBuckets()) {
                for (Terms.Bucket mostBucket : mostEventCount.getBuckets()) {
                    return new long[]{mostBucket.getDocCount(), leastBucket.getDocCount()};
                }
            }
            throw new RuntimeException("Error retrieving event counts");
        } else {
            throw new RuntimeException("Error connecting to the cluster");
        }
    }

    public long repoEventCount(String repo, OpenSearchUtil openSearchUtil) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", repo));
        SearchRequest searchRequest = new SearchRequest("github_events");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(0);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        RestStatus status = searchResponse.status();
        if (status == RestStatus.OK) {
            return searchResponse.getHits().getTotalHits().value;
        } else {
            throw new RuntimeException("Error connecting to the cluster");
        }
    }

    public double[] getSlopeAndIntercept(double x0, double y0, double x1, double y1) {
        if (x1 - x0 != 0) {
            double m = (y1 - y0) / (x1 - x0);
            double b = y1 - m * x1;
            return new double[]{m, b};
        }
        return null;
    }

    public long inactivityLinEq(double[] slopeAndIntercept, double x, double lowerBound) {
        if (slopeAndIntercept == null) {
            return Math.round(lowerBound);
        } else if (slopeAndIntercept.length == 2) {
            double m = slopeAndIntercept[0];
            double b = slopeAndIntercept[1];
            return Math.round(m * x + b);
        }
        throw new RuntimeException("Slope and Intercept set wrong");
    }

    public boolean calculateInactivity(long currentRepoEventCount, double[] slopeAndIntercept, double lowerBound, EventData eventData) {
        long daysUntilInactive = inactivityLinEq(slopeAndIntercept, (double) currentRepoEventCount, lowerBound);
        Duration durationUntilInactive = Duration.ofDays(daysUntilInactive);
        Instant benchmark = Instant.now().minus(durationUntilInactive);

        return eventData.getTimeLastEngaged().isBefore(benchmark);
    }

    public List<MaintainerData> repoMaintainers(String repo) {
        String rawMaintainersFile = String.format("https://raw.githubusercontent.com/opensearch-project/%s/main/MAINTAINERS.md", repo);
        boolean isEmeritusSection = false;
        List<MaintainerData> maintainersList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(rawMaintainersFile).openStream(),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("|")) {
                    String[] columns = line.split("\\|");
                    if (columns.length >= 4) {
                        String maintainer = columns[1].trim();
                        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
                        Matcher matcher = pattern.matcher(columns[2]);
                        String githubId = matcher.find() ? matcher.group(1) : "";
                        String affiliation = columns[3].trim();
                        if (!isEmeritusSection && !maintainer.toLowerCase().contains("emeritus") && !githubId.isEmpty()) {
                            MaintainerData maintainerData = new MaintainerData();
                            maintainerData.setRepository(repo);
                            maintainerData.setName(maintainer);
                            maintainerData.setGithubLogin(githubId);
                            maintainerData.setAffiliation(affiliation);
                            maintainersList.add(maintainerData);
                        }
                    }
                } else if (line.contains("Emeritus")) {
                    isEmeritusSection = true;
                } else if (!line.isEmpty() && isEmeritusSection) {
                    isEmeritusSection = false;
                }
            }
        } catch (FileNotFoundException e) {
            return maintainersList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maintainersList;
    }
}
