package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * @author root
 */
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveEnrollmentDetails {
    @JsonProperty("ARCNo")
    String arcNo;

    @JsonProperty("Photo")
    String photo;

    @JsonProperty("PhotoCompressed")
    String photoCompressed;

    @JsonProperty("EnrollmentStationID")
    String enrollmentStationID;

    @JsonProperty("EnrollmentStationUnitID")
    String enrollmentStationUnitID;

    @JsonProperty("EnrollmentStatus")
    String enrollmentStatus;

    @JsonProperty("EnrollmentDate")
    String enrollmentDate;

    @JsonProperty("FP")
    Set<FP> fp = new HashSet<>();

    @JsonProperty("IRIS")
    Set<IRIS> iris = new HashSet<>();

    @JsonProperty("IRISScannerSerailNo")
    String iRISScannerSerailNo;

    @JsonProperty("LeftFPScannerSerailNo")
    String leftFPScannerSerailNo;

    @JsonProperty("RightFPScannerSerailNo")
    String rightFPScannerSerailNo;

    @JsonProperty("UniqueID")
    String uniqueID;

    @JsonProperty("ARCStatus")
    String arcStatus;

    //For Biometric Options
    @JsonProperty("BiometricOptions")
    String biometricOptions;

}
