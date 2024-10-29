/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearchmetrics.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AbstractBaseLambda implements RequestHandler<Void, Void> {

    @Override
    public Void handleRequest(Void input, Context context) {
        context.getLogger().log("Hello, Abstract Lambda!");
        return null;
    }
}
