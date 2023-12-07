package org.opensearchhealth.model

import com.fasterxml.jackson.annotation.JsonInclude
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
class HealthScoreConfig {
}