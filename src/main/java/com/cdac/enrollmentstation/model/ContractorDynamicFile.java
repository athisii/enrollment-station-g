package com.cdac.enrollmentstation.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractorDynamicFile {
    String dynamicUserCategoryId;
    String dynamicIssuanceUnit;
    String dynamicContractorId;
    String accessDetailsFromDate;
    String accessDetailsToDate;
    String accessPermissionUnitCode;
    String accessPermissionZoneId;
    String accessPermissionWorkingCode;
    String labourPhoto;
    String labourFpPos;
    String labourFpData;
    String signatureFile1;
    String signatureFile3;
}
