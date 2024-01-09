package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Padmanabhan
 */

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetail {
    String arcNo;
    String name;
    String rank;
    String applicantId;
    String unit;
    @JsonProperty("signatureReqd")
    boolean signatureRequired;
    List<String> fingers;
    List<String> iris;
    String detailLink;
    String arcStatus;
    int errorCode;
    String desc;
    String emailId;
    String biometricOptions;
}
