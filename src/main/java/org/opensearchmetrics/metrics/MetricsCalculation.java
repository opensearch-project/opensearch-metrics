/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearchmetrics.metrics.general.*;
import org.opensearchmetrics.metrics.label.LabelMetrics;
import org.opensearchmetrics.metrics.release.CodeCoverage;
import org.opensearchmetrics.metrics.maintainer.MaintainerMetrics;
import org.opensearchmetrics.metrics.release.ReleaseInputs;
import org.opensearchmetrics.metrics.release.ReleaseMetrics;
import org.opensearchmetrics.model.codecov.CodeCovResponse;
import org.opensearchmetrics.model.codecov.CodeCovResult;
import org.opensearchmetrics.model.label.LabelData;
import org.opensearchmetrics.model.general.MetricsData;
import org.opensearchmetrics.model.label.LabelData;
import org.opensearchmetrics.model.maintainer.LatestEventData;
import org.opensearchmetrics.model.maintainer.MaintainerData;
import org.opensearchmetrics.model.release.ReleaseMetricsData;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final MaintainerMetrics maintainerMetrics;


    public MetricsCalculation(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper,
                              UntriagedIssues untriagedIssues, UncommentedPullRequests uncommentedPullRequests,
                              UnlabelledPullRequests unlabelledPullRequests, UnlabelledIssues unlabelledIssues,
                              MergedPullRequests mergedPullRequests, OpenPullRequests openPullRequests,
                              OpenIssues openIssues, ClosedIssues closedIssues,
                              CreatedIssues createdIssues, IssueComments issueComments,
                              PullComments pullComments, IssuePositiveReactions issuePositiveReactions,
                              IssueNegativeReactions issueNegativeReactions, LabelMetrics labelMetrics,
                              ReleaseMetrics releaseMetrics, MaintainerMetrics maintainerMetrics) {
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
        this.maintainerMetrics = maintainerMetrics;
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
                        .flatMap(releaseInput -> releaseMetrics.getReleaseRepos(releaseInput.getFullVersion()).entrySet().stream()
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
                            releaseMetricsData.setReleaseNotes(releaseMetrics.getReleaseNotes(releaseInput.getFullVersion(), repoName, releaseInput.getBranch()));
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

    public void generateCodeCovMetrics() {
        ReleaseInputs[] releaseInputs = ReleaseInputs.getAllReleaseInputs();
        Map<String, String> metricFinalData =
                Arrays.stream(releaseInputs)
                        .filter(ReleaseInputs::getTrack)
                        .flatMap(releaseInput -> releaseMetrics.getReleaseRepos(releaseInput.getFullVersion()).entrySet().stream()
                                .flatMap(entry -> {
                                    String repoName = entry.getKey();
                                    String componentName = entry.getValue();
                                    CodeCovResult codeCovResult = new CodeCovResult();
                                    codeCovResult.setRepository(repoName);
                                    codeCovResult.setComponent(componentName);
                                    codeCovResult.setCurrentDate(currentDate.toString());
                                    try {
                                        codeCovResult.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                                .digest(("codecov-metrics-" + releaseInput.getBranch() + releaseInput.getVersion() + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repoName)
                                                        .getBytes()))));
                                    } catch (NoSuchAlgorithmException e) {
                                        throw new RuntimeException(e);
                                    }
                                    codeCovResult.setReleaseVersion(releaseInput.getVersion());
                                    codeCovResult.setVersion(releaseInput.getVersion());
                                    codeCovResult.setReleaseState(releaseInput.getState());
                                    codeCovResult.setBranch(releaseInput.getBranch());
                                    CodeCovResponse codeCovResponse = releaseMetrics.getCodeCoverage(releaseInput.getBranch(), repoName);
                                    codeCovResult.setCommitId(codeCovResponse.getCommitId());
                                    codeCovResult.setState(codeCovResponse.getState());
                                    codeCovResult.setCoverage(codeCovResponse.getCoverage());
                                    codeCovResult.setUrl(codeCovResponse.getUrl());
                                    return Stream.of(codeCovResult);
                                }))
                        .collect(Collectors.toMap(CodeCovResult::getId,
                                codeCovResult -> codeCovResult.getJson(codeCovResult, objectMapper)));
        String codeCovIndexName = "opensearch-codecov-metrics-" + currentDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        openSearchUtil.createIndexIfNotExists(codeCovIndexName);
        openSearchUtil.bulkIndex(codeCovIndexName, metricFinalData);
    }

    public void generateMaintainerMetrics(List<String> repositories) {
        long[] mostAndLeastRepoEventCounts = maintainerMetrics.mostAndLeastRepoEventCounts(openSearchUtil);
        final double mostRepoEventCount = (double) mostAndLeastRepoEventCounts[0];
        final double leastRepoEventCount = (double) mostAndLeastRepoEventCounts[1];
        final double higherBoundDays = 365; // 1 year
        final double lowerBoundDays = 90; // 3 months

        // Slope and intercept for linear equation:
        // x = number of events
        // y = time maintainer is inactive until they are flagged as inactive
        final double[] slopeAndIntercept = maintainerMetrics.getSlopeAndIntercept(leastRepoEventCount, higherBoundDays, mostRepoEventCount, lowerBoundDays);

        List<String> eventTypes = maintainerMetrics.getEventTypes(openSearchUtil);

        Map<String, String> metricFinalData = repositories.stream()
                .flatMap(repo -> {
                    long currentRepoEventCount = maintainerMetrics.repoEventCount(repo, openSearchUtil);
                    return maintainerMetrics.repoMaintainers(repo).stream()
                            .flatMap(maintainerData -> {
                                // latestEvent will keep track of the latest of all event types
                                LatestEventData latestEvent = null;

                                // List of documents that represent each particular event type(issues, pull_request, label, etc.)
                                List<MaintainerData> individualEvents = new ArrayList<>();

                                // Loop through each event type(issues, pull_request, label, etc.)
                                for (String eventType : eventTypes) {
                                    MaintainerData maintainerEvent = new MaintainerData(); // doc to be indexed

                                    // setting values for doc
                                    try {
                                        maintainerEvent.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                                .digest(("maintainer-inactivity-" + eventType + "-" + maintainerData.getGithubLogin() + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repo)
                                                        .getBytes()))));
                                    } catch (NoSuchAlgorithmException e) {
                                        throw new RuntimeException(e);
                                    }
                                    maintainerEvent.setCurrentDate(currentDate.toString());
                                    maintainerEvent.setEventType(eventType);
                                    maintainerEvent.setRepository(repo);
                                    maintainerEvent.setName(maintainerData.getName());
                                    maintainerEvent.setGithubLogin((maintainerData.getGithubLogin()));
                                    maintainerEvent.setAffiliation(maintainerData.getAffiliation());

                                    // Query for the latest event of the current event type(issues, pull_request, label, etc.)
                                    Optional<LatestEventData> latestEventDataOpt = maintainerMetrics.queryLatestEvent(repo, maintainerData.getGithubLogin(), eventType, openSearchUtil);

                                    if (latestEventDataOpt.isPresent()) { // If an event was found in the query
                                        LatestEventData currentLatestEvent = latestEventDataOpt.get();

                                        // calculate inactivity for current event type
                                        currentLatestEvent.setInactive(maintainerMetrics.calculateInactivity(currentRepoEventCount, slopeAndIntercept, lowerBoundDays, currentLatestEvent));

                                        // Logic to keep track of latest event of all event types.
                                        if (latestEvent != null) {
                                            if (currentLatestEvent.getTimeLastEngaged().isAfter(latestEvent.getTimeLastEngaged())) {
                                                latestEvent = currentLatestEvent;
                                            }
                                        } else { // first time it is run
                                            latestEvent = currentLatestEvent;
                                        }

                                        // continue setting values for doc
                                        maintainerEvent.setEventAction(currentLatestEvent.getEventAction());
                                        maintainerEvent.setTimeLastEngaged(currentLatestEvent.getTimeLastEngaged().toString());
                                        maintainerEvent.setInactive(currentLatestEvent.isInactive());
                                    } else {
                                        // If no event was found in query, then leave event action and time last engaged empty,
                                        // and set inactive to true
                                        maintainerEvent.setInactive(true);
                                    }

                                    individualEvents.add(maintainerEvent);
                                }

                                // Index an extra document that represents a combination of all event types
                                maintainerData.setEventType("All");

                                // Set values for this document
                                try {
                                    maintainerData.setId(String.valueOf(UUID.nameUUIDFromBytes(MessageDigest.getInstance("SHA-1")
                                            .digest(("maintainer-inactivity-" + maintainerData.getEventType() + "-" + maintainerData.getGithubLogin() + "-" + currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + repo)
                                                    .getBytes()))));
                                } catch (NoSuchAlgorithmException e) {
                                    throw new RuntimeException(e);
                                }
                                maintainerData.setCurrentDate(currentDate.toString());

                                // Set values based on latest event of all event types
                                if (latestEvent != null) {
                                    maintainerData.setEventAction(latestEvent.getEventType() + "." + latestEvent.getEventAction()); // e.g. issues.opened
                                    maintainerData.setTimeLastEngaged(latestEvent.getTimeLastEngaged().toString());
                                    maintainerData.setInactive(latestEvent.isInactive());
                                } else {
                                    maintainerData.setInactive(true);
                                }
                                Stream<MaintainerData> compositeEvent = Stream.of(maintainerData);
                                return Stream.concat(individualEvents.stream(), compositeEvent);
                            });
                })
                .collect(Collectors.toMap(MaintainerData::getId, maintainerData -> maintainerData.getJson(maintainerData, objectMapper)));
        String indexName = "maintainer-inactivity-" + currentDate.format(DateTimeFormatter.ofPattern("MM-yyyy"));
        openSearchUtil.createIndexIfNotExists(indexName);
        openSearchUtil.bulkIndex(indexName, metricFinalData);
    }
}
