/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.junit.jupiter.api.Test;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearchmetrics.util.OpenSearchUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReleaseScheduleFetcherTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 13);

    @Test
    void testGetReleaseInputsMapsScheduleEntries() {
        OpenSearchUtil openSearchUtil = openSearchUtilReturning(
                scheduleEntry("3.9.0", ReleaseInputs.STATUS_ACTIVE, "2026-09-29"),
                scheduleEntry("3.8.0", ReleaseInputs.STATUS_RELEASED, "2026-08-04"),
                scheduleEntry("4.0.0", ReleaseInputs.STATUS_INACTIVE, "2027-03-01"));

        List<ReleaseInputs> releaseInputs = new ReleaseScheduleFetcher().getReleaseInputs(TODAY, openSearchUtil);

        // The inactive 4.0.0 is ignored entirely; only active and released versions are mapped.
        assertEquals(2, releaseInputs.size());
        // Only the active release is tracked: 3.8.0 shipped more than the grace window ago.
        List<String> tracked = releaseInputs.stream()
                .filter(ReleaseInputs::getTrack)
                .map(ReleaseInputs::getVersion)
                .collect(Collectors.toList());
        assertEquals(List.of("3.9.0"), tracked);
        assertEquals(ReleaseInputs.STATE_CLOSED, releaseInputs.get(1).getState());
    }

    @Test
    void testGetReleaseInputsSkipsUnusableEntriesWithoutFailingTheBatch() {
        OpenSearchUtil openSearchUtil = openSearchUtilReturning(
                scheduleEntry("3.9.0", ReleaseInputs.STATUS_ACTIVE, "2026-09-29"),
                scheduleEntry("not-a-version", ReleaseInputs.STATUS_ACTIVE, "2026-09-29"),
                scheduleEntry(null, ReleaseInputs.STATUS_ACTIVE, "2026-09-29"),
                scheduleEntry("3.10.0", null, "2026-11-01"));

        List<ReleaseInputs> releaseInputs = new ReleaseScheduleFetcher().getReleaseInputs(TODAY, openSearchUtil);

        assertEquals(1, releaseInputs.size());
        assertEquals("3.9.0", releaseInputs.get(0).getVersion());
    }

    @Test
    void testGetReleaseInputsHandlesAMissingReleaseDate() {
        OpenSearchUtil openSearchUtil = openSearchUtilReturning(
                scheduleEntry("3.9.0", ReleaseInputs.STATUS_ACTIVE, null));

        List<ReleaseInputs> releaseInputs = new ReleaseScheduleFetcher().getReleaseInputs(TODAY, openSearchUtil);

        assertEquals(1, releaseInputs.size());
        assertTrue(releaseInputs.get(0).getTrack());
    }

    @Test
    void testEmptyScheduleThrows() {
        // An empty index means the register job never ran, which must not look like "nothing to track".
        OpenSearchUtil openSearchUtil = openSearchUtilReturning();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> new ReleaseScheduleFetcher().getReleaseInputs(TODAY, openSearchUtil));
        assertTrue(exception.getMessage().contains(ReleaseScheduleFetcher.RELEASE_SCHEDULE_INDEX));
    }

    private static SearchHit scheduleEntry(String version, String status, String releaseDate) {
        Map<String, Object> source = new HashMap<>();
        source.put("version", version);
        source.put("status", status);
        source.put("release_date", releaseDate);
        SearchHit hit = mock(SearchHit.class);
        when(hit.getSourceAsMap()).thenReturn(source);
        return hit;
    }

    private static OpenSearchUtil openSearchUtilReturning(SearchHit... hits) {
        SearchHits searchHits = mock(SearchHits.class);
        when(searchHits.getHits()).thenReturn(hits);
        SearchResponse searchResponse = mock(SearchResponse.class);
        when(searchResponse.getHits()).thenReturn(searchHits);
        OpenSearchUtil openSearchUtil = mock(OpenSearchUtil.class);
        when(openSearchUtil.search(any(SearchRequest.class))).thenReturn(searchResponse);
        return openSearchUtil;
    }
}
