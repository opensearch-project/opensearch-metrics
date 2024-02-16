package org.opensearchhealth.metrics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public final class Repo {

    @JsonProperty("title")
    private String title;

    @JsonProperty("number")
    private long number;
}
