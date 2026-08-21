/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearchmetrics.util.OpenSearchUtil;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reads the release schedule that the {@code release-schedule-register} Jenkins job registers daily, so the
 * metrics workflow discovers which versions to report on at runtime instead of from a hardcoded list.
 */
public class ReleaseScheduleFetcher {

    static final String RELEASE_SCHEDULE_INDEX = "opensearch_release_schedule";

    /** The schedule holds one document per release; a few hundred covers every version ever scheduled. */
    private static final int MAX_SCHEDULE_ENTRIES = 100;

    @Inject
    public ReleaseScheduleFetcher() {}

    /**
     * @return the inputs for every scheduled release, including versions that are not currently tracked;
     *         callers filter on {@link ReleaseInputs#getTrack()}
     */
    public List<ReleaseInputs> getReleaseInputs(LocalDate today, OpenSearchUtil openSearchUtil) {
        SearchRequest searchRequest = new SearchRequest(RELEASE_SCHEDULE_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(MAX_SCHEDULE_ENTRIES);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = openSearchUtil.search(searchRequest);
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits.length == 0) {
            throw new RuntimeException(String.format("No release schedule entries found in %s. Check that the "
                    + "release-schedule-register job is running.", RELEASE_SCHEDULE_INDEX));
        }
        return Arrays.stream(hits)
                .map(SearchHit::getSourceAsMap)
                .map(source -> ReleaseInputs.fromSchedule(
                        asString(source.get("version")),
                        asString(source.get("status")),
                        asString(source.get("release_date")),
                        today))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
