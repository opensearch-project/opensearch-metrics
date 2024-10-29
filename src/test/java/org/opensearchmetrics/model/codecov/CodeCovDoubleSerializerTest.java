/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearchmetrics.model.codecov;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.verify;

public class CodeCovDoubleSerializerTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    private CodeCovDoubleSerializer serializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        serializer = new CodeCovDoubleSerializer();
    }

    @Test
    public void testSerializeNullValue() throws IOException {
        serializer.serialize(null, jsonGenerator, serializerProvider);
        verify(jsonGenerator).writeNumber(0.0);
    }

    @Test
    public void testSerializeNonNullValue() throws IOException {
        Double value = 85.5;
        serializer.serialize(value, jsonGenerator, serializerProvider);
        verify(jsonGenerator).writeNumber(85.5);
    }

    @Test
    public void testSerializeZeroValue() throws IOException {
        Double value = 0.0;
        serializer.serialize(value, jsonGenerator, serializerProvider);
        verify(jsonGenerator).writeNumber(0.0);
    }
}
