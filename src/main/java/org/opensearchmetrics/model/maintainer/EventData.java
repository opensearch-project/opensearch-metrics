package org.opensearchmetrics.model.maintainer;

import lombok.Data;

import java.time.Instant;

@Data
public class EventData {
    private String eventAction;
    private Instant timeLastEngaged;
    private boolean inactive;
}
