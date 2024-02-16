package org.opensearchhealth.util;

import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenSearchUtil {
    private static final int NUM_REPLICAS = 2;
    private static final int FIELD_MAPPING_LIMIT = 8000;
    private static final int NUM_THREADS = 8;
    private static final int OS_BULK_SIZE = 200;
    private static final int INDEX_THREAD_TIMEOUT_MINUTES = 10;

    private final RestHighLevelClient client;

    public OpenSearchUtil(RestHighLevelClient client) {
        this.client = client;
    }

    public void createIndexIfNotExists(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        if (!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            createIndexRequest.settings(Settings.builder()
                    .put("index.number_of_replicas", NUM_REPLICAS)
                    .put("index.mapping.total_fields.limit", FIELD_MAPPING_LIMIT)
                    .put("index.mapping.depth.limit", 300)
                    .put("index.mapping.nested_fields.limit", 800)
                    .put("index.mapping.nested_objects.limit", FIELD_MAPPING_LIMIT)
                    .build());
            System.out.println("Creating index " + index);
            CreateIndexResponse createIndexResponse =
                    client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            System.out.println("Create index " + createIndexResponse.index() + ", acknowledged = " + createIndexResponse.isAcknowledged() + ", shard acknowledged = " + createIndexResponse.isShardsAcknowledged());
        } else {
            System.out.println("Index " + index + " already exists, skip creating index.");
        }
    }

    /*public long processScrolling(String scrollId, long cumulativeTotalHits) throws IOException {
        while (true) {
            SearchResponse nextSearchResponse = client.scroll(
                    new SearchScrollRequest(scrollId).scroll(TimeValue.timeValueMinutes(2)),
                    RequestOptions.DEFAULT
            );

            if (nextSearchResponse.getHits().getHits().length == 0) {
                break;
            }

            cumulativeTotalHits += nextSearchResponse.getHits().getTotalHits().value; // Increment the cumulative count
            scrollId = nextSearchResponse.getScrollId();
        }
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        return cumulativeTotalHits;
    }*/

    public void createIndexAlias(String index, String alias) {
        try {
            IndicesAliasesRequest request = new IndicesAliasesRequest();
            IndicesAliasesRequest.AliasActions aliasAction =
                    new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                            .index(index)
                            .alias(alias);
            request.addAliasAction(aliasAction);
            log.info("Creating alias {} for index {}", alias, index);
            AcknowledgedResponse indicesAliasesResponse =
                    client.indices().updateAliases(request, RequestOptions.DEFAULT);
            log.info("Create alias = {} for index = {}, shard acknowledged = {}", alias, index, indicesAliasesResponse.isAcknowledged());
        } catch (IOException e) {
            throw new RuntimeException("failed to update aliases with request:", e);
        }
    }

    /**
     * Bulk index json data into an OpenSearch index.
     *
     * @param index   name of the index
     * @param jsonMap key/value pair where key is the id of the doc and value is the json string
     * @throws Exception if indexing failed
     */
    public void bulkIndex(@NonNull String index, @NonNull Map<String, String> jsonMap) throws Exception {
        if (jsonMap.isEmpty()) {
            System.out.println("Empty data received for indexing");
            return;
        }
        var forkJoinPool = new ForkJoinPool(NUM_THREADS);
        Set<Map.Entry<String, String>> entries = jsonMap.entrySet();
        var partitions = Iterables.partition(entries, Math.max(1, entries.size() / NUM_THREADS));
        forkJoinPool.submit(() -> partitions.forEach(p -> bulkIndexInBatches(index, p)))
                .get(INDEX_THREAD_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    private void bulkIndexInBatches(String index, List<Map.Entry<String, String>> partition) {
        System.out.println("Started bulk indexing...");
        BulkRequest bulkRequest = new BulkRequest();
        for (Map.Entry<String, String> entry : partition) {
            bulkRequest.add(new IndexRequest()
                    .index(index)
                    .id(entry.getKey())
                    .source(entry.getValue(), XContentType.JSON));
            if (bulkRequest.numberOfActions() >= OS_BULK_SIZE) {
                execBulkRequest(bulkRequest, client);
                bulkRequest = new BulkRequest();
            }
        }
        if (bulkRequest.numberOfActions() > 0) {
            execBulkRequest(bulkRequest, client);
        }
        System.out.println("Bulk indexing finished on thread");
    }

    private static void execBulkRequest(BulkRequest bulkRequest, RestHighLevelClient client) {
        try {
            BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (response.hasFailures()) {
                System.out.println("Bulk index has errors: " + response.buildFailureMessage());
            }
        } catch (IOException ioException) {
            System.out.println ("Error " + ioException);
        }
    }

    public void deleteDocument(String issueIndex, String docId) {
        DeleteRequest request = new DeleteRequest(
                issueIndex,
                docId);
        try {
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            if (response.getResult() == DocWriteResponse.Result.NOT_FOUND) {
                log.error("Index removal failed - document not found by id: {}", docId);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Failed to remove the document:", e);
        }
    }

    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
}
