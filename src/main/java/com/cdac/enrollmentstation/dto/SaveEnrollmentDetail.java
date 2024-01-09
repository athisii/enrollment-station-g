package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * @author root
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveEnrollmentDetail {
    String arcNo;
    String photo;
    String photoCompressed;
    String enrollmentStationId;
    String enrollmentStationUnitId;
    String enrollmentStatus;
    String enrollmentDate;
    Set<Fp> fp;
    Set<Iris> iris;
    String irisScannerSerialNo;
    String leftFpScannerSerialNo;
    String rightFpScannerSerialNo;
    String uniqueId;
    String arcStatus;
    String biometricOptions;
    String signature;
    String signatureCompressed;
    @JsonProperty("signatureReqd")
    boolean signatureRequired;
}
