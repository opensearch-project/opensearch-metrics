/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * The release versions are now derived at runtime from the release schedule that the
 * release-schedule-register Jenkins job publishes daily to opensearch_release_schedule
 * so a release opens and closes on its own schedule with no code change.
 */
public class ReleaseInputs {

    public static final String STATE_OPEN = "open";
    public static final String STATE_CLOSED = "closed";

    /** In flight: within the schedule's activation window of the RC date. */
    static final String STATUS_ACTIVE = "active";
    /** The release date has passed. */
    static final String STATUS_RELEASED = "released";
    /** Scheduled, but too far out to track yet. */
    static final String STATUS_INACTIVE = "inactive";

    /** How long a shipped release keeps reporting, in its closed state, before it drops off. */
    static final long RELEASED_TRACKING_GRACE_DAYS = 1;

    private static final String BRANCH_MAIN = "main";

    private final String version;
    private final String state;
    private final String branch;
    private final boolean track;

    ReleaseInputs(String version, String state, String branch, boolean track) {
        this.version = version;
        this.state = state;
        this.branch = branch;
        this.track = track;
    }

    public String getVersion() {
        return version;
    }

    public String getState() {
        return state;
    }

    public String getBranch() {
        return branch;
    }

    public boolean getTrack() {
        return track;
    }

    /**
     * Translates one release schedule entry into the inputs the metrics workflow needs.
     *
     * <p>An active version reports as open and is tracked. A released version reports as closed and keeps
     * being tracked for 1 day past its release date. Every other
     * status (for example inactive or unrecognized) is ignored
     *
     */
    public static Optional<ReleaseInputs> fromSchedule(String version, String status, String releaseDate,
                                                       LocalDate today) {
        String[] versionParts = versionParts(version);
        if (versionParts == null || status == null) {
            return Optional.empty();
        }
        boolean released = STATUS_RELEASED.equals(status);
        if (!released && !STATUS_ACTIVE.equals(status)) {
            // Inactive or unrecognized statuses are ignored entirely rather than reported as open.
            return Optional.empty();
        }
        String state = released ? STATE_CLOSED : STATE_OPEN;
        boolean track = released ? withinReleasedGrace(releaseDate, today) : true;
        return Optional.of(new ReleaseInputs(version.trim(), state, deriveBranch(versionParts, state), track));
    }

    /**
     * An open major/minor release is cut from main branch, patch releases and already-released major/minor
     * versions reports against its own branch.
     */
    private static String deriveBranch(String[] versionParts, String state) {
        boolean minorRelease = "0".equals(versionParts[2]);
        return minorRelease && STATE_OPEN.equals(state)
                ? BRANCH_MAIN
                : versionParts[0] + "." + versionParts[1];
    }

    private static boolean withinReleasedGrace(String releaseDate, LocalDate today) {
        return parseDate(releaseDate)
                .map(date -> !today.isAfter(date.plusDays(RELEASED_TRACKING_GRACE_DAYS)))
                .orElse(false);
    }

    /** Handles both the {@code yyyy-MM-dd} the schedule registers and a full timestamp. */
    private static Optional<LocalDate> parseDate(String value) {
        if (value == null || value.length() < 10) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value.substring(0, 10)));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static String[] versionParts(String version) {
        if (version == null) {
            return null;
        }
        String[] parts = version.trim().split("\\.");
        if (parts.length != 3) {
            return null;
        }
        for (String part : parts) {
            if (part.isEmpty() || !part.chars().allMatch(Character::isDigit)) {
                return null;
            }
        }
        return parts;
    }
}
