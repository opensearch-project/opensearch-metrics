package org.opensearchhealth.dagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearchhealth.metrics.MetricsCalculation;
import org.opensearchhealth.util.OpenSearchUtil;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import javax.inject.Singleton;

@Module
public class CommonModule {

    public static final String OPENSEARCH_DOMAIN_ENDPOINT = "OPENSEARCH_DOMAIN_ENDPOINT";
    private static final String OPENSEARCH_DOMAIN_REGION = "OPENSEARCH_DOMAIN_REGION";
    private static final String OPENSEARCH_DOMAIN_ROLE = "OPENSEARCH_DOMAIN_ROLE";
    private static final String ROLE_SESSION_NAME = "OpenSearchHealth";

    @Singleton
    @Provides
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        return objectMapper;
    }

    private AwsCredentialsProvider stsAssumeRoleCredentialProvider(String roleSessionName, String assumeRoleArn) {
        StsClient stsClient = StsClient.create();
        AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                .stsClient(stsClient)
                .refreshRequest(b -> b.roleArn(assumeRoleArn).roleSessionName(roleSessionName))
                .build();

        return credentialsProvider;
    }

    @Singleton
    @Provides
    public RestHighLevelClient getOpenSearchHLClient() {
        final String region = System.getenv(OPENSEARCH_DOMAIN_REGION);
        final String assumeRoleArn = System.getenv(OPENSEARCH_DOMAIN_ROLE);
        final AwsCredentialsProvider awsCredentialsProvider = stsAssumeRoleCredentialProvider(ROLE_SESSION_NAME, assumeRoleArn);
        String serviceName = "es";
        HttpRequestInterceptor interceptor = new AwsRequestSigningApacheInterceptor(serviceName, Aws4Signer.create(), awsCredentialsProvider, region);
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(System.getenv(OPENSEARCH_DOMAIN_ENDPOINT), 443, "https"))
                        .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.addInterceptorLast(interceptor)));
    }

    @Singleton
    @Provides
    public OpenSearchUtil getOpenSearchUtil(RestHighLevelClient client) {
        return new OpenSearchUtil(client);
    }

    @Provides
    @Singleton
    public MetricsCalculation getGitHubHealthCalculation(OpenSearchUtil openSearchUtil, ObjectMapper objectMapper) {
        return new MetricsCalculation(openSearchUtil, objectMapper);
    }
}
