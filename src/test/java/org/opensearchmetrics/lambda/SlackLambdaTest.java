package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearchmetrics.metrics.MetricsCalculation;
import org.opensearchmetrics.util.OpenSearchUtil;
import org.opensearchmetrics.datasource.DataSourceType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.opensearchmetrics.util.SecretsManagerUtil;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;

import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class SlackLambdaTest {
    @Mock
    private SecretsManagerUtil secretsManagerUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleRequest() throws IOException{
        SlackLambda slackLambda = new SlackLambda(secretsManagerUtil);

        Optional optional = mock(Optional.class);
        SNSEvent snsEvent = getSNSEventFromMessage("");
        Context context = mock(Context.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        HttpClientBuilder httpClientBuilder = mock(HttpClientBuilder.class);
        StatusLine statusLine = mock(StatusLine.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        InputStream inputStream = mock(InputStream.class);

        when(secretsManagerUtil.getSlackCredentials(any(DataSourceType.class))).thenReturn(optional);
        when(optional.get()).thenReturn("");

        try (var mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class)) {
            mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(httpClientBuilder);
            when(httpClientBuilder.build()).thenReturn(httpClient);
            when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
            when(httpResponse.getStatusLine()).thenReturn(statusLine);
            when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            when(httpResponse.getEntity()).thenReturn(httpEntity);
            when(httpEntity.getContent()).thenReturn(inputStream);

            slackLambda.handleRequest(snsEvent, context);
        }


        // Assert
        verify(httpClient, times(1)).execute(any(HttpPost.class));
        verify(secretsManagerUtil, times(3)).getSlackCredentials(any(DataSourceType.class));
    }

    @Test
    public void testHandleRequestWithSecretManagerUtilException() throws IOException{
        SlackLambda slackLambda = new SlackLambda(secretsManagerUtil);

        Optional optional = mock(Optional.class);
        SNSEvent snsEvent = getSNSEventFromMessage("");
        Context context = mock(Context.class);

        when(optional.get()).thenReturn("");

        doThrow(new IOException("Error running getSlackCredentials")).when(secretsManagerUtil).getSlackCredentials(any(DataSourceType.class));

        try {
            slackLambda.handleRequest(snsEvent, context);
            fail("Expected a RuntimeException to be thrown");
        } catch (RuntimeException e) {
            // Exception caught as expected
            System.out.println("Caught exception message: " + e.getMessage());
            assertTrue(e.getMessage().contains("Error running getSlackCredentials"));
        }
    }

    @Test
    public void testHandleRequestWithHttpClientException() throws IOException{
        SlackLambda slackLambda = new SlackLambda(secretsManagerUtil);

        Optional optional = mock(Optional.class);
        SNSEvent snsEvent = getSNSEventFromMessage("");
        Context context = mock(Context.class);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        HttpClientBuilder httpClientBuilder = mock(HttpClientBuilder.class);
        StatusLine statusLine = mock(StatusLine.class);
        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        InputStream inputStream = mock(InputStream.class);

        when(secretsManagerUtil.getSlackCredentials(any(DataSourceType.class))).thenReturn(optional);
        when(optional.get()).thenReturn("");

        try (var mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class)) {
            mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(httpClientBuilder);
            when(httpClientBuilder.build()).thenReturn(httpClient);
            doThrow(new IOException("Error running httpClient execute")).when(httpClient).execute(any(HttpPost.class));

            try {
                slackLambda.handleRequest(snsEvent, context);
                fail("Expected a RuntimeException to be thrown");
            } catch (RuntimeException e) {
                // Exception caught as expected
                System.out.println("Caught exception message: " + e.getMessage());
                assertTrue(e.getMessage().contains("Error running httpClient execute"));
            }
        }
    }

    private SNSEvent getSNSEventFromMessage(String message) throws IOException {
        SNSEvent event = new SNSEvent();
        SNSEvent.SNSRecord record = new SNSEvent.SNSRecord();
        record.setSns(new SNSEvent.SNS().withMessage(message));
        event.setRecords(List.of(record));
        return event;
    }
}
