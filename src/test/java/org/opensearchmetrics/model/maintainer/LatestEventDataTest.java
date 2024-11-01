/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.maintainer;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LatestEventDataTest {
    private LatestEventData latestEventData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        latestEventData = new LatestEventData();
    }

    @Test
    public void testEventType() {
        latestEventData.setEventType("testType");
        assertEquals("testType", latestEventData.getEventType());
    }

    @Test
    public void testEventAction() {
        latestEventData.setEventAction("testAction");
        assertEquals("testAction", latestEventData.getEventAction());
    }

    @Test
    public void testTimeLastEngaged() {
        Instant testInstant = Instant.parse("2021-02-09T11:19:42.12Z");
        latestEventData.setTimeLastEngaged(testInstant);
        assertEquals(testInstant, latestEventData.getTimeLastEngaged());
    }

    @Test
    public void testInactive() {
        latestEventData.setInactive(true);
        assertEquals(true, latestEventData.isInactive());
    }
}
