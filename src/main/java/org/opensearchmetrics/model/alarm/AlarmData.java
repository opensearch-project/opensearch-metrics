package org.opensearchmetrics.model.alarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AlarmData {
    @JsonProperty("AlarmName")
    private String alarmName;
    @JsonProperty("AlarmDescription")
    private String alarmDescription;
    @JsonProperty("StateChangeTime")
    private String stateChangeTime;
    @JsonProperty("Region")
    private String region;
    @JsonProperty("AlarmArn")
    private String alarmArn;
}
