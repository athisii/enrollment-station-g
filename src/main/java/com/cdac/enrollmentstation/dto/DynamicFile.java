package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class DynamicFile {
    String userCategoryId;
    String labourName;
    String dateOfBirth;
    int genderId;
    int bloodGroupId;
    int nationalityId;
    String issuanceUnit;
    String labourId;
    String contractorId;
}
