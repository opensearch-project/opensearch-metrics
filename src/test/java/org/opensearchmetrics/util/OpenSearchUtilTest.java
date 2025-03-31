/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest;


import java.io.IOException;
import java.util.Map;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class OpenSearchUtilTest {

    private OpenSearchUtil openSearchUtil;

    @Mock
    private RestHighLevelClient client;

    @Mock
    private IndicesClient indicesClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        openSearchUtil = new OpenSearchUtil(client);
    }

    @Test
    void WHEN_index_exists_THEN_doNothing() throws IOException {
        when(client.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(GetIndexRequest.class), any())).thenReturn(true);
        openSearchUtil.createIndexIfNotExists("some_index", Optional.empty());

        verify(indicesClient, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    void WHEN_index_not_exist_THEN_create_index_along_with_alias() throws IOException {

        when(client.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(GetIndexRequest.class), any(RequestOptions.class))).thenReturn(false);
        when(indicesClient.create(any(CreateIndexRequest.class), any(RequestOptions.class)))
                .thenReturn(new CreateIndexResponse(true, true, "some_index"));
        when(indicesClient.updateAliases(any(IndicesAliasesRequest.class), any(RequestOptions.class)))
                .thenReturn(new AcknowledgedResponse(true));
        openSearchUtil.createIndexIfNotExists("some_index", Optional.of("maintainer-activity"));

        verify(indicesClient).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indicesClient).create(any(CreateIndexRequest.class), any(RequestOptions.class));
        verify(indicesClient).updateAliases(any(IndicesAliasesRequest.class), any(RequestOptions.class));
        verifyNoMoreInteractions(indicesClient);
    }

    @Test
    void WHEN_index_not_exist_THEN_create_index_along_with_alias_exception() throws IOException {
        when(client.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(GetIndexRequest.class), any(RequestOptions.class))).thenReturn(false);
        when(indicesClient.create(any(CreateIndexRequest.class), any(RequestOptions.class)))
                .thenReturn(new CreateIndexResponse(true, true, "some_index"));

        doThrow(new IOException("Error adding alias to index")).when(indicesClient).updateAliases(any(IndicesAliasesRequest.class), any(RequestOptions.class));

        try {
            openSearchUtil.createIndexIfNotExists("some_index", Optional.of("maintainer-activity"));
            fail("Expected a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Error adding alias to index"));
        }
    }

    @Test
    void GIVEN_index_AND_data_THEN_bulkIndex() throws Exception {
        BulkResponse mockResponse = Mockito.mock(BulkResponse.class);
        when(client.bulk(Mockito.any(BulkRequest.class), Mockito.any())).thenReturn(mockResponse);
        openSearchUtil.bulkIndex("some_index", Map.of("doc_id", "json_data"));

        verify(client, times(1)).bulk(Mockito.any(), Mockito.any());
    }

    @Test
    void GIVEN_index_AND_empty_data_THEN_no_bulkIndex() throws Exception {
        openSearchUtil.bulkIndex("some_index", Map.of());
        verify(client, times(0)).bulk(Mockito.any(), Mockito.any());
    }
}
