/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.model.maintainer;

import lombok.Data;

import java.time.Instant;

@Data
public class LatestEventData {
    private String eventType;
    private String eventAction;
    private Instant timeLastEngaged;
    private boolean inactive;
}
