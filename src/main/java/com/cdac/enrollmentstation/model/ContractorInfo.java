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
public class ContractorInfo {
    String contractorId;
    String contractorName;
    String serialNo;
    String contractId;
    int cardReaderHandle;
}
