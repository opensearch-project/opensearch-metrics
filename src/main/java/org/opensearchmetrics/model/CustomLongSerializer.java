package org.opensearchmetrics.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CustomLongSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (value == null) {
            return;
        }
        if (value == 0 || value == 0L) {
            jsonGenerator.writeNumber(0);
        } else {
            jsonGenerator.writeNumber(value);
        }
    }
}
