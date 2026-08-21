/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

public class ReleaseInputsTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);

    @Test
    public void testActiveMinorReleaseIsOpenAndTrackedOnMain() {
        Optional<ReleaseInputs> result =
                ReleaseInputs.fromSchedule("3.9.0", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY);
        ReleaseInputs releaseInputs = result.orElseThrow();
        assertEquals("3.9.0", releaseInputs.getVersion());
        assertEquals(ReleaseInputs.STATE_OPEN, releaseInputs.getState());
        assertEquals("main", releaseInputs.getBranch());
        assertTrue(releaseInputs.getTrack());
        // Unlike an ignored release, an active one produces a present, tracked result.
        assertEquals(Optional.of(true), result.map(ReleaseInputs::getTrack));
    }

    @Test
    public void testActivePatchReleaseTracksItsOwnBranch() {
        ReleaseInputs releaseInputs = ReleaseInputs
                .fromSchedule("2.19.7", ReleaseInputs.STATUS_ACTIVE, "2026-09-01", TODAY)
                .orElseThrow();
        assertEquals(ReleaseInputs.STATE_OPEN, releaseInputs.getState());
        assertEquals("2.19", releaseInputs.getBranch());
        assertTrue(releaseInputs.getTrack());
    }

    @Test
    public void testReleasedIsClosedAndBranchIsNeverMain() {
        ReleaseInputs releaseInputs = ReleaseInputs
                .fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, TODAY.toString(), TODAY)
                .orElseThrow();
        assertEquals(ReleaseInputs.STATE_CLOSED, releaseInputs.getState());
        assertEquals("3.8", releaseInputs.getBranch());
        // Released today → still within the 1-day grace window, so a closed release keeps reporting.
        // Drop-off after the window is covered by testReleasedIsTrackedThroughTheGraceWindowThenDropsOff.
        assertTrue(releaseInputs.getTrack());
    }

    @Test
    public void testReleasedIsTrackedThroughTheGraceWindowThenDropsOff() {
        // Released today, and on the last day of the grace window, the closed state still reports.
        assertTrue(trackedAsReleased(TODAY));
        assertTrue(trackedAsReleased(TODAY.minusDays(ReleaseInputs.RELEASED_TRACKING_GRACE_DAYS)));
        // One day past the window it stops, so the release leaves the dashboard.
        assertFalse(trackedAsReleased(TODAY.minusDays(ReleaseInputs.RELEASED_TRACKING_GRACE_DAYS + 1)));
        assertFalse(trackedAsReleased(TODAY.minusMonths(6)));
    }

    @Test
    public void testReleasedWithAnUnusableDateIsNotTracked() {
        assertFalse(ReleaseInputs.fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, null, TODAY)
                .orElseThrow().getTrack());
        assertFalse(ReleaseInputs.fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, "", TODAY)
                .orElseThrow().getTrack());
        assertFalse(ReleaseInputs.fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, "not-a-date", TODAY)
                .orElseThrow().getTrack());
    }

    @Test
    public void testReleaseDateAcceptsAFullTimestamp() {
        assertTrue(ReleaseInputs
                .fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, TODAY + "T00:00:00.000Z", TODAY)
                .orElseThrow().getTrack());
    }

    @Test
    public void testInactiveIsIgnored() {
        // Inactive releases are neither active nor released, so they are ignored entirely rather than
        // reported as open.
        Optional<ReleaseInputs> releaseInputs =
                ReleaseInputs.fromSchedule("4.0.0", ReleaseInputs.STATUS_INACTIVE, "2027-03-01", TODAY);
        assertEquals(Optional.empty(), releaseInputs);
        // Being ignored, it produces no tracked release.
        assertTrue(releaseInputs.map(ReleaseInputs::getTrack).isEmpty());
    }

    @Test
    public void testUnrecognisedStatusIsIgnored() {
        // Anything the schedule starts emitting that this code does not know about is ignored rather than
        // being guessed at.
        Optional<ReleaseInputs> releaseInputs =
                ReleaseInputs.fromSchedule("3.9.0", "cancelled", "2026-09-29", TODAY);
        assertEquals(Optional.empty(), releaseInputs);
        // Being ignored, it produces no tracked release.
        assertTrue(releaseInputs.map(ReleaseInputs::getTrack).isEmpty());
    }

    @Test
    public void testUnusableScheduleEntriesAreSkipped() {
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule(null, ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY));
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule("3.9.0", null, "2026-09-29", TODAY));
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule("3.9", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY));
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule("3.9.0.1", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY));
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule("3.x.0", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY));
        assertEquals(Optional.empty(), ReleaseInputs.fromSchedule("", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY));
    }

    @Test
    public void testVersionIsTrimmed() {
        assertEquals("3.9.0", ReleaseInputs
                .fromSchedule(" 3.9.0 ", ReleaseInputs.STATUS_ACTIVE, "2026-09-29", TODAY)
                .orElseThrow().getVersion());
    }

    private boolean trackedAsReleased(LocalDate releaseDate) {
        return ReleaseInputs.fromSchedule("3.8.0", ReleaseInputs.STATUS_RELEASED, releaseDate.toString(), TODAY)
                .orElseThrow().getTrack();
    }
}
