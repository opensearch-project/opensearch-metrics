/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.metrics.release;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlResponse {


    @Inject
    public UrlResponse() {}

    public HttpURLConnection getUrlResponse (String url) {
        try {
            URL responseUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) responseUrl.openConnection();
            connection.setRequestMethod("HEAD");
            return connection;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
