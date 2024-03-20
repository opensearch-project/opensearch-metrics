package org.opensearchmetrics.dagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import org.opensearchmetrics.metrics.general.*;
import org.opensearchmetrics.metrics.label.LabelMetrics;
import org.opensearchmetrics.metrics.release.ReleaseBranchChecker;
import org.opensearchmetrics.metrics.release.ReleaseLabelIssuesFetcher;
import org.opensearchmetrics.metrics.release.ReleaseLabelPullsFetcher;
import org.opensearchmetrics.metrics.release.ReleaseMetrics;
import org.opensearchmetrics.metrics.release.ReleaseNotesChecker;
import org.opensearchmetrics.metrics.release.ReleaseRepoFetcher;
import org.opensearchmetrics.metrics.release.ReleaseVersionIncrementChecker;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Named;
import javax.inject.Singleton;
@Module
public class MetricsModule {

    public static final String UNTRIAGED_ISSUES = "Untriaged Issues";
    public static final String UNCOMMENTED_PULL_REQUESTS = "Uncommented Pull Requests";
    public static final String UNLABELLED_PULL_REQUESTS = "Unlabelled Pull Requests";

    public static final String UNLABELLED_ISSUES = "Unlabelled Issues";

    public static final String MERGED_PULL_REQUESTS = "Merged Pull Requests";

    public static final String OPEN_PULL_REQUESTS = "Open Pull Requests";

    public static final String OPEN_ISSUES = "Open Issues";

    public static final String CLOSED_ISSUES = "Closed Issues";

    public static final String CREATED_ISSUES = "Created Issues";

    public static final String ISSUE_COMMENTS = "Issue Comments";
    public static final String PULL_COMMENTS = "Pull Comments";

    public static final String ISSUE_POSITIVE_REACTIONS = "Issue Positive Reactions";

    public static final String ISSUE_NEGATIVE_REACTIONS = "Issue Negative Reactions";
    @Provides
    @Singleton
    @Named(UNTRIAGED_ISSUES)
    public Metrics getUntriagedIssues() {
        return new UntriagedIssues();
    }

    @Provides
    @Singleton
    @Named(UNCOMMENTED_PULL_REQUESTS)
    public Metrics getUncommentedPullRequests() {
        return new UncommentedPullRequests();
    }

    @Provides
    @Singleton
    @Named(UNLABELLED_PULL_REQUESTS)
    public Metrics getUnlabelledPullRequests() {
        return new UnlabelledPullRequests();
    }

    @Provides
    @Singleton
    @Named(UNLABELLED_ISSUES)
    public Metrics getUnlabelledIssues() {
        return new UnlabelledIssues();
    }

    @Provides
    @Singleton
    @Named(MERGED_PULL_REQUESTS)
    public Metrics getMergedPullRequests() {
        return new MergedPullRequests();
    }

    @Provides
    @Singleton
    @Named(OPEN_PULL_REQUESTS)
    public Metrics getOpenPullRequests() {
        return new OpenPullRequests();
    }

    @Provides
    @Singleton
    @Named(OPEN_ISSUES)
    public Metrics getOpenIssues() {
        return new OpenIssues();
    }

    @Provides
    @Singleton
    @Named(CLOSED_ISSUES)
    public Metrics getClosedIssues() {
        return new ClosedIssues();
    }

    @Provides
    @Singleton
    @Named(CREATED_ISSUES)
    public Metrics getCreatedIssues() {
        return new CreatedIssues();
    }

    @Provides
    @Singleton
    @Named(ISSUE_COMMENTS)
    public Metrics getIssueComments() {
        return new IssueComments();
    }

    @Provides
    @Singleton
    @Named(PULL_COMMENTS)
    public Metrics getPullComments() {
        return new PullComments();
    }

    @Provides
    @Singleton
    @Named(ISSUE_POSITIVE_REACTIONS)
    public Metrics getPIssuePositiveReactions() {
        return new IssuePositiveReactions();
    }

    @Provides
    @Singleton
    @Named(ISSUE_NEGATIVE_REACTIONS)
    public Metrics getIssueNegativeReactions() {
        return new IssueNegativeReactions();
    }

    @Provides
    @Singleton
    public LabelMetrics getLabelMetrics() {
        return new LabelMetrics();
    }

    @Provides
    @Singleton
    public ReleaseMetrics getReleaseMetrics(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper,
                                            ReleaseRepoFetcher releaseRepoFetcher, ReleaseLabelIssuesFetcher releaseLabelIssuesFetcher,
                                            ReleaseLabelPullsFetcher releaseLabelPullsFetcher, ReleaseVersionIncrementChecker releaseVersionIncrementChecker,
                                            ReleaseBranchChecker releaseBranchChecker, ReleaseNotesChecker releaseNotesChecker) {
        return new ReleaseMetrics(openSearchUtil, objectMapper, releaseRepoFetcher, releaseLabelIssuesFetcher, releaseLabelPullsFetcher, releaseVersionIncrementChecker, releaseBranchChecker, releaseNotesChecker);
    }
}
