package org.opensearchmetrics.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        openSearchUtil.createIndexIfNotExists("some_index");

        verify(indicesClient, times(0)).create(any(CreateIndexRequest.class), any(RequestOptions.class));
    }

    @Test
    void WHEN_index_not_exist_THEN_create_index() throws IOException {

        when(client.indices()).thenReturn(indicesClient);
        when(indicesClient.exists(any(GetIndexRequest.class), any(RequestOptions.class))).thenReturn(false);
        when(indicesClient.create(any(CreateIndexRequest.class), any(RequestOptions.class)))
                .thenReturn(new CreateIndexResponse(true, true, "some_index"));
        openSearchUtil.createIndexIfNotExists("some_index");

        verify(indicesClient).exists(any(GetIndexRequest.class), any(RequestOptions.class));
        verify(indicesClient).create(any(CreateIndexRequest.class), any(RequestOptions.class));
        verifyNoMoreInteractions(indicesClient);
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
