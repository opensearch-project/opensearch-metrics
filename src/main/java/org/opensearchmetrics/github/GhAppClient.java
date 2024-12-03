/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.github;

import org.opensearchmetrics.datasource.DataSourceType;
import org.opensearchmetrics.model.github.GhAppAccessToken;
import org.opensearchmetrics.util.SecretsManagerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@VisibleForTesting
public class GhAppClient {

    protected static final String GH_ISSUE_BASE_URL = "https://api.github.com";
    private final SecretsManagerUtil secretsManagerUtil;
    private final ObjectMapper objectMapper;

    public GhAppClient(SecretsManagerUtil secretsManagerUtil, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.secretsManagerUtil = secretsManagerUtil;
    }

    protected HttpResponse executeGet(HttpGet request, CloseableHttpClient client) {
        try {
            return client.execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Error while making HTTP call", e);
        }
    }

    @VisibleForTesting
    CloseableHttpClient createJwtClient(String token) {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        try {
            Header contentTypeHeader = new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
            Header authorizationHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            List<Header> headers = Arrays.asList(contentTypeHeader, authorizationHeader);
            httpClientBuilder.setDefaultHeaders(headers);
        } catch (Exception ex) {
            log.info("Unable to get GitHub credentials, making non-authenticated API calls", ex);
        }
        return httpClientBuilder.build();
    }

    HttpResponse executePost(HttpPost request, CloseableHttpClient client) {
        try {
            return client.execute(request);
        } catch (IOException e) {
            throw new RuntimeException("Error while making HTTP call", e);
        }
    }

     /*
    Private key of the GH App (.pem format) -> DER (byte conversion) -> JWT -> access tokens as ghs_fmdsknfefefefdehdedfehdF
    */

    // TOKEN:2 Method used to get the access token from JWT token
    @VisibleForTesting
    public String createAccessToken() throws Exception {
        String privateKeyDER = secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_KEY).get();
        String ghAppJWT = createJWT(secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_ID).get(),
                privateKeyDER);
        String accessTokenUrl = GH_ISSUE_BASE_URL + "/app/installations/"
                + secretsManagerUtil.getGitHubAppCredentials(DataSourceType.GITHUB_APP_INSTALL_ID).get()
                + "/access_tokens";
        HttpPost request = new HttpPost(accessTokenUrl);
        CloseableHttpClient createJwtClient = createJwtClient(ghAppJWT);
        HttpResponse response = executePost(request, createJwtClient(ghAppJWT));
        BasicResponseHandler basicResponseHandler = new BasicResponseHandler();
        String data = basicResponseHandler.handleResponse(response);
        GhAppAccessToken accessToken = objectMapper.readValue(data, GhAppAccessToken.class);
        createJwtClient.close();
        return accessToken.getToken();
    }

    // TOKEN:1 method used to convert the private key to DER format and outputs the JWT token
    @VisibleForTesting
    String createJWT(String issuer, String privateKeyDER) throws Exception {
        byte[] data = Base64.decodeBase64(privateKeyDER);
        ASN1EncodableVector asn1EncodableVectorV1 = new ASN1EncodableVector();
        asn1EncodableVectorV1.add(new ASN1Integer(0));
        ASN1EncodableVector asn1EncodableVectorV2 = new ASN1EncodableVector();
        asn1EncodableVectorV2.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
        asn1EncodableVectorV2.add(DERNull.INSTANCE);
        asn1EncodableVectorV1.add(new DERSequence(asn1EncodableVectorV2));
        asn1EncodableVectorV1.add(new DEROctetString(data));
        ASN1Sequence asn1Sequence = new DERSequence(asn1EncodableVectorV1);
        byte[] privKey = asn1Sequence.getEncoded("DER");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new  PKCS8EncodedKeySpec(privKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        long currentTimeMillis = System.currentTimeMillis();
        Date issueDate = new Date(currentTimeMillis);
        long expiryTimeMillis = currentTimeMillis + 600000;
        Date expDate = new Date(expiryTimeMillis);
        String compactJws = Jwts.builder()
                .subject("GhIssueDelete")
                .issuer(issuer)
                .issuedAt(issueDate)
                .expiration(expDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return compactJws;
    }
}
