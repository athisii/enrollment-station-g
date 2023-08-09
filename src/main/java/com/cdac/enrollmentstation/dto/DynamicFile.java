package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
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
