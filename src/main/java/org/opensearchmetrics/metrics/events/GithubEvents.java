/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.events;

import lombok.Getter;

@Getter
public enum GithubEvents {
    ISSUES_OPENED("issues.opened"),
    ISSUES_CLOSED("issues.closed"),
    ISSUES_LABELED("issues.labeled"),
    ISSUES_UNLABELED("issues.unlabeled"),
    ISSUES_TRANSFERRED("issues.transferred"),
    ISSUES_ASSIGNED("issues.assigned"),
    ISSUE_COMMENT_CREATED("issue_comment.created"),
    PULL_REQUEST_CLOSED("pull_request.closed"),
    PULL_REQUEST_OPENED("pull_request.opened"),
    PULL_REQUEST_LABELED("pull_request.labeled"),
    PULL_REQUEST_UNLABELED("pull_request.unlabeled"),
    PULL_REQUEST_ASSIGNED("pull_request.assigned"),
    PULL_REQUEST_REVIEW_SUBMITTED("pull_request_review.submitted"),
    PULL_REQUEST_REVIEW_COMMENT_CREATED("pull_request_review_comment.created"),
    GOLLUM("gollum");

    private final String eventName;

    GithubEvents(String eventName) {
        this.eventName = eventName;
    }

    public static GithubEvents[] getAllGithubEvents() {
        return values();
    }
}
