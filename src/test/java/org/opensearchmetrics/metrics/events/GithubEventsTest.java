/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GithubEventsTest {
    @Test
    public void testGetEventName() {
        assertEquals("issues.opened", GithubEvents.ISSUES_OPENED.getEventName());
        assertEquals("issues.closed", GithubEvents.ISSUES_CLOSED.getEventName());
        assertEquals("issues.labeled", GithubEvents.ISSUES_LABELED.getEventName());
        assertEquals("issues.unlabeled", GithubEvents.ISSUES_UNLABELED.getEventName());
        assertEquals("issues.transferred", GithubEvents.ISSUES_TRANSFERRED.getEventName());
        assertEquals("issues.assigned", GithubEvents.ISSUES_ASSIGNED.getEventName());
        assertEquals("issue_comment.created", GithubEvents.ISSUE_COMMENT_CREATED.getEventName());
        assertEquals("pull_request.closed", GithubEvents.PULL_REQUEST_CLOSED.getEventName());
        assertEquals("pull_request.opened", GithubEvents.PULL_REQUEST_OPENED.getEventName());
        assertEquals("pull_request.labeled", GithubEvents.PULL_REQUEST_LABELED.getEventName());
        assertEquals("pull_request.unlabeled", GithubEvents.PULL_REQUEST_UNLABELED.getEventName());
        assertEquals("pull_request.assigned", GithubEvents.PULL_REQUEST_ASSIGNED.getEventName());
        assertEquals("pull_request_review.submitted", GithubEvents.PULL_REQUEST_REVIEW_SUBMITTED.getEventName());
        assertEquals("pull_request_review_comment.created", GithubEvents.PULL_REQUEST_REVIEW_COMMENT_CREATED.getEventName());
        assertEquals("gollum", GithubEvents.GOLLUM.getEventName());
    }

    @Test
    public void testGetAllGithubEvents() {
        GithubEvents[] githubEvents = GithubEvents.getAllGithubEvents();
        assertEquals(15, githubEvents.length);
        assertEquals(GithubEvents.ISSUES_OPENED, githubEvents[0]);
        assertEquals(GithubEvents.ISSUES_CLOSED, githubEvents[1]);
        assertEquals(GithubEvents.ISSUES_LABELED, githubEvents[2]);
        assertEquals(GithubEvents.ISSUES_UNLABELED, githubEvents[3]);
        assertEquals(GithubEvents.ISSUES_TRANSFERRED, githubEvents[4]);
        assertEquals(GithubEvents.ISSUES_ASSIGNED, githubEvents[5]);
        assertEquals(GithubEvents.ISSUE_COMMENT_CREATED, githubEvents[6]);
        assertEquals(GithubEvents.PULL_REQUEST_CLOSED, githubEvents[7]);
        assertEquals(GithubEvents.PULL_REQUEST_OPENED, githubEvents[8]);
        assertEquals(GithubEvents.PULL_REQUEST_LABELED, githubEvents[9]);
        assertEquals(GithubEvents.PULL_REQUEST_UNLABELED, githubEvents[10]);
        assertEquals(GithubEvents.PULL_REQUEST_ASSIGNED, githubEvents[11]);
        assertEquals(GithubEvents.PULL_REQUEST_REVIEW_SUBMITTED, githubEvents[12]);
        assertEquals(GithubEvents.PULL_REQUEST_REVIEW_COMMENT_CREATED, githubEvents[13]);
        assertEquals(GithubEvents.GOLLUM, githubEvents[14]);
    }
}
