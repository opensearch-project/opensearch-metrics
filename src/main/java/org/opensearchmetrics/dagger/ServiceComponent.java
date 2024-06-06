package org.opensearchmetrics.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Component;
import org.opensearchmetrics.metrics.MetricsCalculation;
import org.opensearchmetrics.metrics.general.Metrics;
import org.opensearchmetrics.metrics.label.LabelMetrics;
import org.opensearchmetrics.util.OpenSearchUtil;
import org.opensearchmetrics.util.SecretsManagerUtil;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = {CommonModule.class, MetricsModule.class})
public interface ServiceComponent {

    ObjectMapper getObjectMapper();

    OpenSearchUtil getOpenSearchUtil();

    MetricsCalculation getMetricsCalculation();

    SecretsManagerUtil getSecretsManagerUtil();

    @Named(MetricsModule.UNTRIAGED_ISSUES)
    Metrics getUntriagedIssues();

    @Named(MetricsModule.UNCOMMENTED_PULL_REQUESTS)
    Metrics getUncommentedPullRequests();

    @Named(MetricsModule.UNLABELLED_ISSUES)
    Metrics getUnlabelledPullRequests();

    @Named(MetricsModule.UNLABELLED_PULL_REQUESTS)
    Metrics getUnlabelledIssues();
    @Named(MetricsModule.MERGED_PULL_REQUESTS)
    Metrics getMergedPullRequests();

    @Named(MetricsModule.OPEN_PULL_REQUESTS)
    Metrics getOpenPullRequests();

    @Named(MetricsModule.ISSUE_COMMENTS)
    Metrics getIssueComments();

    @Named(MetricsModule.ISSUE_POSITIVE_REACTIONS)
    Metrics getIssuePositiveReactions();

    @Named(MetricsModule.ISSUE_NEGATIVE_REACTIONS)
    Metrics getIssueNegativeReactions();

    LabelMetrics getLabelMetrics();
}
