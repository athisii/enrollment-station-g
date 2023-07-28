package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Padmanabhan
 */

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetails {
    @JsonProperty("arcNo")
    String arcNo;

    @JsonProperty("Name")
    String name;

    @JsonProperty("Rank")
    String rank;

    @JsonProperty("ApplicantID")
    String applicantID;

    @JsonProperty("Unit")
    String unit;

    @JsonProperty("Fingers")
    List<String> fingers;

    @JsonProperty("Iris")
    List<String> iris;

    @JsonProperty("DetailLink")
    String detailLink;

    @JsonProperty("ArcStatus")
    String arcStatus;

    @JsonProperty("ErrorCode")
    String errorCode;

    @JsonProperty("Desc")
    String desc;

    @JsonProperty("EmailID")
    String emailId;
    @JsonProperty("BiometricOptions")
    String biometricOptions;
}
