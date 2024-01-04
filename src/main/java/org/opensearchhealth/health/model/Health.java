package org.opensearchhealth.health.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public final class Health {

    @JsonProperty("id")
    private String id;

    @JsonProperty("current_date")
    private String currentDate;
    @JsonProperty("repository")
    private String repository;
    @JsonProperty("themes")
    private List<Theme> themes;

    @JsonProperty("action_items")
    private List<String> actionItems;

    @Data
    public static class Theme {
        @JsonProperty("theme_name")
        private String name;
        @JsonProperty("theme_description")
        private String themeDescription;
        @JsonProperty("factors")
        private List<Factor> factors;
    }

    @Data
    public static class Factor {
        @JsonProperty("factor_name")
        private String name;

        @JsonProperty("factor_description")
        private String factorDescription;

        @JsonSerialize(using = CustomLongSerializer.class)
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @JsonProperty("factor_value")
        private long count;

        @JsonProperty("factor_string_value")
        private String factorStringValue;

        @JsonSerialize(using = CustomLongSerializer.class)
        @JsonProperty("factor_threshold")
        private long allowedValue;

        @JsonProperty("factor_status")
        private String thresholdStatus;
    }

     public String toJson(ObjectMapper mapper) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("current_date", currentDate);
        data.put("repository", repository);
        data.put("themes", themes);
        data.put("action_items", actionItems);
        return mapper.writeValueAsString(data);
    }
}

class CustomLongSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (value == null) {
            // If the value is null, simply return without serializing
            return;
        }
        if (value == 0 || value == 0L) {
            jsonGenerator.writeNumber(0);
        } else {
            jsonGenerator.writeNumber(value);
        }
    }
}
