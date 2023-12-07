package org.opensearchhealth.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AbstractBaseLambda implements RequestHandler<Void, Void> {

    @Override
    public Void handleRequest(Void input, Context context) {
        context.getLogger().log("Hello, Abstract Lambda!");
        return null;
    }
}
