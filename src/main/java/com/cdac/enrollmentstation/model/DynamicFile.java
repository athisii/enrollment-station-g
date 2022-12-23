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
public class DynamicFile {
    String userCategoryId;
    String labourName;
    String labourGenderId;
    String labourDateOfBirth;

    String labourBloodGroupId;

    String labourNationalityId;
    String issuanceUnit;
    String labourId;
    String contractorId;

}
