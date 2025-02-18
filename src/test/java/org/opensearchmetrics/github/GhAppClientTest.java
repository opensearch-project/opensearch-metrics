/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.github;

import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.opensearchmetrics.datasource.DataSourceType;
import org.opensearchmetrics.model.github.GhAppAccessToken;
import org.opensearchmetrics.util.SecretsManagerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GhAppClientTest {

    private GhAppClient ghAppClient;
    @Mock
    private SecretsManagerUtil secretsManagerUtil;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.ghAppClient = new GhAppClient(secretsManagerUtil, objectMapper);
    }

    @Test
    void WHEN_executeGet_THEN_return_CloseableHttpClient() {
        HttpGet httpGet = mock(HttpGet.class);
        HttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        when(ghAppClient.executeGet(httpGet, closeableHttpClient)).thenReturn(closeableHttpResponse);
    }

    @Test
    void WHEN_executePost_THEN_return_CloseableHttpClient() {
        HttpPost httpPost = mock(HttpPost.class);
        HttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        CloseableHttpClient closeableHttpClient = mock(CloseableHttpClient.class);
        when(ghAppClient.executePost(httpPost, closeableHttpClient)).thenReturn(closeableHttpResponse);
    }

    @Test
    public void WHEN_createJwtClient_THEN_return_jwtClient() throws NoSuchFieldException, IllegalAccessException {
        String testToken = "test-jwt-token";
        CloseableHttpClient client = ghAppClient.createJwtClient(testToken);
        assertNotNull(client);
    }

    @Test
    public void WHEN_createAccessToken_THEN_return_accessToken() throws Exception {
        // Mock dependencies
        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);

        // Setup test data
        String testPrivateKey = "test-private-key";
        String testAppId = "test-app-id";
        String testInstallId = "test-install-id";
        String testJwt = "test-jwt";
        String testResponseData = "{\"token\": \"test-access-token\"}";
        GhAppAccessToken testAccessToken = new GhAppAccessToken();
        testAccessToken.setToken("test-access-token");

        // Setup mock behaviors
        when(secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_KEY))
                .thenReturn(Optional.of(testPrivateKey));
        when(secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_ID))
                .thenReturn(Optional.of(testAppId));
        when(secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_INSTALL_ID))
                .thenReturn(Optional.of(testInstallId));

        GhAppClient spyGhAppClient = spy(new GhAppClient(secretsManagerUtil, objectMapper));
        doReturn(testJwt).when(spyGhAppClient).createJWT(anyString(), anyString());
        doReturn(mockHttpClient).when(spyGhAppClient).createJwtClient(anyString());
        doReturn(mockResponse).when(spyGhAppClient).executePost(any(HttpPost.class), any(CloseableHttpClient.class));

        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

        when(mockResponse.getEntity()).thenReturn(new StringEntity(testResponseData));
        when(objectMapper.readValue(testResponseData, GhAppAccessToken.class))
                .thenReturn(testAccessToken);

        // Execute the method
        String result = spyGhAppClient.createAccessToken();

        // Verify the result and interactions
        assertEquals("test-access-token", result);
        verify(secretsManagerUtil).getGitHubAppCredentials(DataSourceType.GITHUB_APP_KEY);
        verify(secretsManagerUtil).getGitHubAppCredentials(DataSourceType.GITHUB_APP_ID);
        verify(secretsManagerUtil).getGitHubAppCredentials(DataSourceType.GITHUB_APP_INSTALL_ID);
        verify(spyGhAppClient).createJWT(testAppId, testPrivateKey);
        verify(mockHttpClient).close();
    }

    @Test
    void WHEN_createJWT_THEN_return_jwt() throws Exception {
        String testKey = "MIIEpAIBAAKCAQEAtoRdC4mwHyGm3ZnhEnU5x1FqnrP61epGW00lXx9UA4yOXxjV\n" +
                "F5FZ/8m5i4oS5mv4q3lWEteXmML/b17fopYvUjkWiFdJf94kpxWcVvm4I0xyeqYE\n" +
                "9ASjpN2jNjQ8s/u03WpytHewsp5/aWoiZtAGBTfBvVyVPXFS31/cS/jY+0Z33P4n\n" +
                "MKBbqb3CbdPz4rd/VWDWOI1LhXBexkcKZcyPROebF3RejxzkxgNhzeC3huErT6mX\n" +
                "nJ3NGXWydMCQG+SVjg54g9A9i8yJ0ue+RgiIxnu5LLjncDiXZZpy/UdcABedejB3\n" +
                "TgJGzX5Za2W+k1rTVF10FliIOJ2QIWUuIrEsyQIDAQABAoIBAQCNLtBmp2hUfIx+\n" +
                "aJTg2UsLcmA+SUykAmfQIlnhPfOYFzbeOvBDHc13foyHcxPxp92gjuhVBO4gXd6H\n" +
                "QOVO+Eu8l6plZtfVEHpbwOzBnsOgkncPhrLYK2qGkme4+ylltDQQ/lGiZd+KG+7F\n" +
                "FTNtQkcV7C5yk1ZiQ/HuFlHrdqAppedkqUunT23ohw+R10AOMV20laVpf/nrOfO3\n" +
                "lfoXmr/NUIj83rziC8dJ15+2HqpEmTHO+w7aUka14ZG9OnLUVwpD/dHuLkNrk0Me\n" +
                "KffbxDb3163k03JkeL8lWQs19PQgcjpxF4JZk7r/kPQ/TwP8inedsN1FeMIP8aZS\n" +
                "LEjihE+BAoGBAN9Li9AG3HuIL1+bCXz5oxvjszYHkbJa5Ksi+kTbOEILFhk8aMcB\n" +
                "+bSWVLkgF7EMc+W2407nL/YY7dCnbqBs7rRbQ0+sVKYeL8LglVnMWaZYH/FnKQ52\n" +
                "QF+Dv9PXynd1w/PQujAKn1MMGSvxPEW16sGnXrH1E4AJlheBUEUOllYZAoGBANE/\n" +
                "1+P5VkndOjcgvuQPUI1ogD4+maUjZVVj/z5a48S4GOPKITtOvRrMFI5heH0wEjYw\n" +
                "XYqwBSMRCDrYp+tGqZLzlGUPiN/g6pDgSd5Id6QUsNxgn+ekOSFlHNjZwAEXBdNY\n" +
                "tKvrrQ9r/jz+cggE6OpKoKUuybAOU/1VzQ50AoIxAoGAYqhiUbt2Vy5IoBlEC+/Q\n" +
                "XVYxrEGT4hW+ys5dfWbOaH+1d9j1AlihF2UEcfb4AMXbvzcbH5WN31IMYRBZFJCM\n" +
                "tytLhjxB+lOEDrpjwpVDVvfAxUwrG7SrpIf1jYfecQGbXnJukSNgWbUSuhOP6c0C\n" +
                "uCVW9ZGu1/dkVWZRLPHRAqECgYEArGGyE0dHhNZRrTS2zd6n97bNX3nmzZqZUn1s\n" +
                "uwvZdChNqOrN8bPuKfNSQ/Gcd1Vwy1+Q0D4uHTNc2k2+GB9Ad6Ve7NqdYgJCe1Oq\n" +
                "xwpgNbYt9X9MfGJYBmDsIOFSQhObYv9C6BbhnUDUU58yhdS1pL4SFcKzuOw02REk\n" +
                "OvHrVyECgYButEdNKoDE47nXa46dtWKW1b5BiWd3HhGUttsResF86O/QExJ0lA0h\n" +
                "5g1hCwej9ZihKazQTWk1cIA6c0/HwLm+KYlqQy8xGKOXtMqyu8y63Ga65QtVdM5c\n" +
                "Bik2ujDXFSRKb5sKGSE1t5I9PWinfMF894apWbB1x9/Zf0CvcUmJ7Q==";
        String createJWT = ghAppClient.createJWT("1234", String.valueOf(testKey));
        assertNotNull(createJWT);
    }
}
