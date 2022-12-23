package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessFileList {

    @JsonProperty("unitCode")
    private String unitCode;

    @JsonProperty("zoneId")
    private String zoneId;

    @JsonProperty("workingHourCode")
    private String workingHourCode;

    @JsonProperty("fromDate")
    private String fromDate;

    @JsonProperty("toDate")
    private String toDate;

}
