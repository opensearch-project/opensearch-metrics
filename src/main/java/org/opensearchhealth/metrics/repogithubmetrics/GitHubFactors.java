package org.opensearchhealth.metrics.repogithubmetrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchhealth.metrics.Factors;
import org.opensearchhealth.metrics.model.CodeCov;
import org.opensearchhealth.metrics.model.MetricsRequest;
import org.opensearchhealth.metrics.model.Repo;
import org.opensearchhealth.util.OpenSearchUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum GitHubFactors implements Factors {

    UNTRIAGED_ISSUES("Untriaged issues", "The total number of issues labeled as untriaged in the repository."),
    GITHUB_AUDIT("GitHub Audit", "Repository Security Audit Status: 1 means noncompliant, and 0 indicates compliant."),
    UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS("Untriaged issues greater than 30 days", "The total number of issues labeled as untriaged in the repository that are older than 30 days."),

    PRS_NOT_RESPONDED("Pull Requests not responded", "The total number of pull requests in the repository that have not received any comments or responses."),
    ISSUES_NOT_RESPONDED_THIRTY_DAYS("Issues older than 30 days and not responded", "The total number of issues in the repository that are older than 30 days and have received no comments or responses."),
    PRS_NOT_RESPONDED_THIRTY_DAYS("Pull Requests older than 30 days and not responded", "The total number of pull requests in the repository that are older than 30 days and have not received any comments or responses."),

    CODECOV_COVERAGE("CodeCov Coverage Percentage", "CodeCov Coverage Score for the Repository.");

    private final String fullName;

    private final String description;

    GitHubFactors(String fullName, String description) {

        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public BoolQueryBuilder getBoolQueryBuilder(MetricsRequest request, LocalDateTime startDate, LocalDateTime endDate) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        switch (this) {
            case GITHUB_AUDIT:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("audit_status.keyword", "noncompliant"));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("current_date").from("now-1d").to("now"));
                }
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                }
                return boolQueryBuilder;
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_labels.keyword", "untriaged"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                }
                return boolQueryBuilder;
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments", 0));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("issue_pull_request", false));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                }
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
                }
                return boolQueryBuilder;
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("state.keyword", "open"));
                    boolQueryBuilder.must(QueryBuilders.matchQuery("comments",0));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("created_at").to("now-29d/d"));
                }
                return boolQueryBuilder;
            case CODECOV_COVERAGE:
                if (request.getRepository() != null) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery("repository.keyword", request.getRepository()));
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("current_date").from("now-1d").to("now"));
                }
                return boolQueryBuilder;
            default:
                throw new RuntimeException("Unknown Github Repo Factor to getBoolQueryBuilder");
        }
    }

    @Override
    public SearchRequest createSearchRequest(MetricsRequest request, BoolQueryBuilder queryBuilder) {
        SearchRequest searchRequest = new SearchRequest(request.getIndex());
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(9000);
        searchRequest.source(searchSourceBuilder);
        switch (this) {
            case GITHUB_AUDIT:
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED:
                return searchRequest;
            case CODECOV_COVERAGE:
                searchSourceBuilder.fetchSource(new String[]{"branch", "coverage"}, null);
                return searchRequest;
            default:
                throw new RuntimeException("Unknown Github Repo Factor to createSearchRequest");
        }
    }

    @Override
    public long performSearch(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        switch (this) {
            case GITHUB_AUDIT:
                if (status == RestStatus.OK) {
                    if(searchResponse.getHits().getTotalHits().value == 0) {
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
            case UNTRIAGED_ISSUES:
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED:
                if (status == RestStatus.OK) {
                    return searchResponse.getHits().getTotalHits().value;
                }
            case CODECOV_COVERAGE:
                if (status == RestStatus.OK) {
                    SearchHits hits = searchResponse.getHits();
                    SearchHit[] searchHits = hits.getHits();
                    return Arrays.stream(searchHits)
                            .map(SearchHit::getSourceAsString)
                            .map(hitJson -> {
                                try {
                                    return objectMapper.readValue(hitJson, CodeCov.class);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .filter(codeCovResult -> codeCovResult.getBranch().equals("main"))
                            .findFirst()
                            .map(CodeCov::getCoverage)
                            .orElse(0L);
                }
            default:
                throw new RuntimeException("Unknown Github Repo Factor to performSearch");
        }
    }

    @Override
    public String getFactorStringValue(long factorValue) {
        switch (this) {
            case GITHUB_AUDIT:
                if(factorValue == 0) {
                    return "compliant";
                } else {
                    return "non-compliant";
                }
            default:
                return null;
        }
    }

    @Override
    public Map<String, Long> performSearchMapValue(OpenSearchUtil opensearchUtil, SearchRequest request, ObjectMapper objectMapper) throws IOException {
        SearchResponse searchResponse = opensearchUtil.search(request);
        RestStatus status = searchResponse.status();
        Map<String, Long> factorMapValue = new HashMap<>();
        switch (this) {
            case UNTRIAGED_ISSUES_GREATER_THAN_THIRTY_DAYS:
            case ISSUES_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED_THIRTY_DAYS:
            case PRS_NOT_RESPONDED:
            case UNTRIAGED_ISSUES:
                if (status == RestStatus.OK) {
                    SearchHits hits = searchResponse.getHits();
                    SearchHit[] searchHits = hits.getHits();
                    for (SearchHit searchHit : searchHits) {
                        String hitJson = searchHit.getSourceAsString();
                        Repo repo = objectMapper.readValue(hitJson, Repo.class);
                        factorMapValue.put(repo.getTitle(), repo.getNumber());
                    }
                }
                if(!factorMapValue.isEmpty()) {
                    return factorMapValue;
                }
            case CODECOV_COVERAGE:
                if (status == RestStatus.OK) {
                    SearchHits hits = searchResponse.getHits();
                    SearchHit[] searchHits = hits.getHits();
                    for (SearchHit searchHit : searchHits) {
                        String hitJson = searchHit.getSourceAsString();
                        CodeCov codeCovResult = objectMapper.readValue(hitJson, CodeCov.class);
                        factorMapValue.put(codeCovResult.getBranch(), codeCovResult.getCoverage());
                    }
                }
                if(!factorMapValue.isEmpty()) {
                    return factorMapValue;
                }
            default:
                return null;
        }
    }
}
