package org.opensearchmetrics.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchmetrics.metrics.general.*;
import org.opensearchmetrics.metrics.label.LabelMetrics;
import org.opensearchmetrics.metrics.release.ReleaseInputs;
import org.opensearchmetrics.metrics.release.ReleaseMetrics;
import org.opensearchmetrics.model.label.LabelData;
import org.opensearchmetrics.model.general.MetricsData;
import org.opensearchmetrics.model.release.ReleaseMetricsData;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MetricsCalculation {

    private final LocalDateTime currentDate;
    private final OpenSearchUtil openSearchUtil;
    private final ObjectMapper objectMapper;
    private final UntriagedIssues untriagedIssues;
    private final UncommentedPullRequests uncommentedPullRequests;
    private final UnlabelledPullRequests unlabelledPullRequests;
    private final UnlabelledIssues unlabelledIssues;
    private final MergedPullRequests mergedPullRequests;
    private final OpenPullRequests openPullRequests;
    private final OpenIssues openIssues;
    private final ClosedIssues closedIssues;
    private final CreatedIssues createdIssues;
    private final IssueComments issueComments;
    private final PullComments pullComments;
    private final IssuePositiveReactions issuePositiveReactions;
    private final IssueNegativeReactions issueNegativeReactions;
    private final LabelMetrics labelMetrics;
    private final ReleaseMetrics releaseMetrics;


    public MetricsCalculation(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper,
                              UntriagedIssues untriagedIssues, UncommentedPullRequests uncommentedPullRequests,
                              UnlabelledPullRequests unlabelledPullRequests, UnlabelledIssues unlabelledIssues,
                              MergedPullRequests mergedPullRequests, OpenPullRequests openPullRequests,
                              OpenIssues openIssues, ClosedIssues closedIssues,
                              CreatedIssues createdIssues, IssueComments issueComments,
                              PullComments pullComments, IssuePositiveReactions issuePositiveReactions,
                              IssueNegativeReactions issueNegativeReactions, LabelMetrics labelMetrics,
                              ReleaseMetrics releaseMetrics) {
        this.unlabelledPullRequests = unlabelledPullRequests;
        this.unlabelledIssues = unlabelledIssues;
        this.mergedPullRequests = mergedPullRequests;
        this.openPullRequests = openPullRequests;
        this.openIssues = openIssues;
        this.closedIssues = closedIssues;
        this.createdIssues = createdIssues;
        this.issueComments = issueComments;
        this.pullComments = pullComments;
        this.issuePositiveReactions = issuePositiveReactions;
        this.issueNegativeReactions = issueNegativeReactions;
        this.currentDate = LocalDateTime.now(ZoneId.of("UTC"));
        this.openSearchUtil = openSearchUtil;
        this.objectMapper = objectMapper;
        this.untriagedIssues = untriagedIssues;
        this.uncommentedPullRequests = uncommentedPullRequests;
        this.labelMetrics = labelMetrics;
        this.releaseMetrics = releaseMetrics;
    }


    public void generateGeneralMetrics(List<String> repositories) {
        List<Metrics> metricsList = Arrays.asList(untriagedIssues, uncommentedPullRequests,
                unlabelledPullRequests, unlabelledIssues,
                mergedPullRequests, openPullRequests,
                openIssues, closedIssues,
                createdIssues, issueComments,
                pullComments, issuePositiveReactions,
                issueNegativeReactions);
        Map<String, String> metricFinalData = repositories.stream()
                .flatMap(repo -> metricsList.stream()
                        .flatMap(metric -> {
                            MetricsData metricsData = new MetricsData();

                            try {
                                metricsData.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                        .digest(("general-metrics-" + metric.toString() + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repo)
                                                .getBytes()))));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            metricsData.setRepository(repo);
                            metricsData.setCurrentDate(currentDate.toString());
                            BoolQueryBuilder boolQueryBuilder = metric.getBoolQueryBuilder(repo);
                            SearchRequest searchRequest = metric.createSearchRequest(boolQueryBuilder, metric.searchIndex());
                            long metricCount = metric.performSearch(openSearchUtil, searchRequest);
                            metricsData.setMetricName(metric.toString());
                            metricsData.setMetricCount(metricCount);
                            return Stream.of(metricsData);
                        }))
                .collect(Collectors.toMap(MetricsData::getId, metricsData -> metricsData.getJson(metricsData, objectMapper)));
        openSearchUtil.createIndexIfNotExists("opensearch_general_metrics");
        openSearchUtil.bulkIndex("opensearch_general_metrics", metricFinalData);
    }

    public void generateLabelMetrics(List<String> repositories) {
        List<LabelMetrics> metricsList = Arrays.asList(labelMetrics);
        Map<String, String> metricFinalData = repositories.stream()
                .flatMap(repo -> metricsList.stream()
                        .flatMap(metric -> {
                            Map<String, List<Long>> labelInfo = null;
                            try {
                                labelInfo = metric.getLabelInfo(repo, openSearchUtil);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return labelInfo.entrySet().stream().flatMap(entry -> {
                                String labelName = entry.getKey();
                                List<Long> values = entry.getValue();
                                LabelData labelData = new LabelData();
                                try {
                                    labelData.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                            .digest(("label-metrics-" + labelName + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repo)
                                                    .getBytes()))));
                                } catch (NoSuchAlgorithmException e) {
                                    throw new RuntimeException(e);
                                }
                                labelData.setRepository(repo);
                                labelData.setCurrentDate(currentDate.toString());
                                labelData.setLabelName(labelName);
                                labelData.setLabelPullCount(values.get(1));
                                labelData.setLabelIssueCount(values.get(0));
                                return Stream.of(labelData);
                            });
                        }))
                .collect(Collectors.toMap(LabelData::getId, labelData -> labelData.getJson(labelData, objectMapper)));
        openSearchUtil.createIndexIfNotExists("opensearch_label_metrics");
        openSearchUtil.bulkIndex("opensearch_label_metrics", metricFinalData);
    }

    public void generateReleaseMetrics() {
        ReleaseInputs[] releaseInputs = ReleaseInputs.getAllReleaseInputs();

        Map<String, String> metricFinalData =
                Arrays.stream(releaseInputs)
                        .filter(ReleaseInputs::getTrack)
                        .flatMap(releaseInput -> releaseMetrics.getReleaseRepos(releaseInput.getVersion()).entrySet().stream()
                        .flatMap(entry -> {
                            String repoName = entry.getKey();
                            String componentName = entry.getValue();
                            ReleaseMetricsData releaseMetricsData = new ReleaseMetricsData();
                            releaseMetricsData.setRepository(repoName);
                            releaseMetricsData.setComponent(componentName);
                            releaseMetricsData.setCurrentDate(currentDate.toString());
                            try {
                                releaseMetricsData.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                        .digest(("release-metrics-" + releaseInput.getVersion() + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repoName)
                                                .getBytes()))));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            releaseMetricsData.setReleaseVersion(releaseInput.getVersion());
                            releaseMetricsData.setVersion(releaseInput.getVersion());
                            releaseMetricsData.setReleaseState(releaseInput.getState());
                            releaseMetricsData.setIssuesOpen(releaseMetrics.getReleaseLabelIssues(releaseInput.getVersion(), repoName, "open", false));
                            releaseMetricsData.setAutocutIssuesOpen(releaseMetrics.getReleaseLabelIssues(releaseInput.getVersion(), repoName, "open", true));
                            releaseMetricsData.setIssuesClosed(releaseMetrics.getReleaseLabelIssues(releaseInput.getVersion(), repoName, "closed", false));
                            releaseMetricsData.setPullsOpen(releaseMetrics.getReleaseLabelPulls(releaseInput.getVersion(), repoName, "open"));
                            releaseMetricsData.setPullsClosed(releaseMetrics.getReleaseLabelPulls(releaseInput.getVersion(), repoName, "closed"));
                            releaseMetricsData.setVersionIncrement(releaseMetrics.getReleaseVersionIncrement(releaseInput.getVersion(), repoName, releaseInput.getBranch()));
                            releaseMetricsData.setReleaseNotes(releaseMetrics.getReleaseNotes(releaseInput.getVersion(), repoName, releaseInput.getBranch()));
                            releaseMetricsData.setReleaseBranch(releaseMetrics.getReleaseBranch(releaseInput.getVersion(), repoName));
                            String[] releaseOwners = releaseMetrics.getReleaseOwners(releaseInput.getVersion(), repoName);
                            releaseMetricsData.setReleaseOwners(releaseOwners);
                            releaseMetricsData.setReleaseOwnerExists(Optional.ofNullable(releaseOwners)
                                    .map(owners -> owners.length > 0)
                                    .orElse(false));
                            String releaseIssue = releaseMetrics.getReleaseIssue(releaseInput.getVersion(), repoName);
                            releaseMetricsData.setReleaseIssue(releaseIssue);
                            releaseMetricsData.setReleaseIssueExists(Optional.ofNullable(releaseIssue)
                                    .map(str -> !str.isEmpty())
                                    .orElse(false));
                            return Stream.of(releaseMetricsData);
                        }))
                .collect(Collectors.toMap(ReleaseMetricsData::getId,
                        releaseMetricsData -> releaseMetricsData.getJson(releaseMetricsData, objectMapper)));
        openSearchUtil.createIndexIfNotExists("opensearch_release_metrics");
        openSearchUtil.bulkIndex("opensearch_release_metrics", metricFinalData);
    }

}
