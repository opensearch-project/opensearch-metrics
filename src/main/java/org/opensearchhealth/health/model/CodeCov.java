package org.opensearchhealth.health.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public final class CodeCov {

    @JsonProperty("branch")
    private String branch;

    @JsonProperty("coverage")
    private long coverage;
}
