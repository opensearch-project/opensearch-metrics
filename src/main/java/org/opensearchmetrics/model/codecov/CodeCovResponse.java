/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.model.codecov;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;

@Data
public class CodeCovResponse {

    @JsonProperty("commitid")
    private String commitId;

    @JsonProperty("url")
    private String url;

    @JsonProperty("state")
    private String state;


    @JsonSerialize(using = CodeCovDoubleSerializer.class)
    @JsonProperty("coverage")
    private Double coverage;
}

class CodeCovDoubleSerializer extends JsonSerializer<Double> {

    @Override
    public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (value == null) {
            jsonGenerator.writeNumber(0.0);
        } else {
            jsonGenerator.writeNumber(value);
        }
    }
}
