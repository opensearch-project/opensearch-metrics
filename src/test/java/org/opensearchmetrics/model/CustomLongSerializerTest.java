package org.opensearchmetrics.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class CustomLongSerializerTest {

    @Test
    void serializeNonNullValue() throws IOException {
        // Arrange
        CustomLongSerializer serializer = new CustomLongSerializer();
        JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        Long value = 12345L;

        // Act
        serializer.serialize(value, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeNumber(value);
    }

    @Test
    void serializeNullValue() throws IOException {
        // Arrange
        CustomLongSerializer serializer = new CustomLongSerializer();
        JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);

        // Act
        serializer.serialize(null, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator, never()).writeNumber(anyLong());
    }

    @Test
    void serializeZeroValue() throws IOException {
        // Arrange
        CustomLongSerializer serializer = new CustomLongSerializer();
        JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);

        // Act
        serializer.serialize(0L, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeNumber(0);
    }

    @Test
    void serializeNonZeroValue() throws IOException {
        // Arrange
        CustomLongSerializer serializer = new CustomLongSerializer();
        JsonGenerator jsonGenerator = mock(JsonGenerator.class);
        SerializerProvider serializerProvider = mock(SerializerProvider.class);
        Long value = 54321L;

        // Act
        serializer.serialize(value, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeNumber(value);
    }
}
