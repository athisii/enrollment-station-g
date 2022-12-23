/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.model;

import RealScan.FP;
import com.cdac.enrollmentstation.model.IRIS;
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

    @JsonProperty("IRISScannerSerialNo")
    String iRISScannerSerialNo;

    @JsonProperty("LeftFPScannerSerialNo")
    String leftFPScannerSerialNo;

    @JsonProperty("RightFPScannerSerialNo")
    String rightFPScannerSerialNo;

    @JsonProperty("UniqueID")
    String uniqueID;

    @JsonProperty("ARCStatus")
    String arcStatus;

    //For Biometric Options
    @JsonProperty("BiometricOptions")
    String biometricOptions;
}
