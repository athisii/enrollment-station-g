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
public class DynamicFileList {
    @JsonProperty("userCategoryID")
    private String userCategoryId;

    @JsonProperty("labourName")
    private String labourName;

    @JsonProperty("dateOfBirth")
    private String labourDateOfBirth;

    @JsonProperty("genderId")
    private String labourGenderId;

    @JsonProperty("bloodGroupId")
    private String labourBloodGroupId;

    @JsonProperty("nationalityId")
    private String labourNationalityId;

    @JsonProperty("issuanceUnit")
    private String issuanceUnit;

    @JsonProperty("labourID")
    private String labourId;

    @JsonProperty("contractorID")
    private String contractorId;
}
