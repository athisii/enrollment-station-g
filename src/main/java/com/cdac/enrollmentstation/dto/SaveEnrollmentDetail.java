package com.cdac.enrollmentstation.dto;

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
}
