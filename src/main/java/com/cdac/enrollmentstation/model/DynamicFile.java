package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("userCategoryID")
    String userCategoryId;

    @JsonProperty("labourName")
    String labourName;

    @JsonProperty("dateOfBirth")
    String labourDateOfBirth;

    @JsonProperty("genderId")
    String labourGenderId;

    @JsonProperty("bloodGroupId")
    String labourBloodGroupId;

    @JsonProperty("nationalityId")
    String labourNationalityId;

    @JsonProperty("issuanceUnit")
    String issuanceUnit;

    @JsonProperty("labourID")
    String labourId;

    @JsonProperty("contractorID")
    String contractorId;
}
