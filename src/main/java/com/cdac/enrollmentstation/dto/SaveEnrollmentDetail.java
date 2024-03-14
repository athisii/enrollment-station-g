package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * @author root
 */
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveEnrollmentDetail {
    @JsonProperty("ARCNo")
    String arcNo;

    @JsonProperty("Photo")
    String photo;

    @JsonProperty("PhotoCompressed")
    String photoCompressed;

    @JsonProperty("EnrollmentStationID")
    String enrollmentStationId;

    @JsonProperty("EnrollmentStationUnitID")
    String enrollmentStationUnitId;

    @JsonProperty("EnrollmentStatus")
    String enrollmentStatus;

    @JsonProperty("EnrollmentDate")
    String enrollmentDate;

    @JsonProperty("Fp")
    Set<Fp> fp;

    @JsonProperty("Iris")
    Set<Iris> iris;

    @JsonProperty("IRISScannerSerialNo") // incorrect name from API
    String irisScannerSerialNo;

    @JsonProperty("LeftFPScannerSerialNo")
    String leftFpScannerSerialNo;

    @JsonProperty("RightFPScannerSerialNo")
    String rightFpScannerSerialNo;

    @JsonProperty("UniqueID")
    String uniqueId;

    @JsonProperty("ARCStatus")
    String arcStatus;

    @JsonProperty("BiometricOptions")
    String biometricOptions;

    String signature = "NA";
    String signatureCompressed = "NA";
}
