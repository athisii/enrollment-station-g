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
public class ContractorDetailsFile {
    String dynamicUserCategoryId;
    String dynamicIssuanceUnit;
    String dynamicContractorId;
    String accessFromDate;
    String accessToDate;
    String accessUnitCode;
    String accessZoneId;
    String accessWorkingHourCode;
    String labourPhoto;
    String labourFpPos;
    String labourFpData;
    String signatureFile1;
    String signatureFile3;
}
