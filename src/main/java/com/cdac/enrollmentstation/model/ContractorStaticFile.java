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
public class ContractorStaticFile {
    byte[] chipSerialNo;
    String cardNo;
    String dateOfBirth;
    String dateIssued;
    String nationality;
    String issuedBy;
    String firmName;
    String gender;
    String userCategoryId;
    String name;
    String uniqueId;

}
