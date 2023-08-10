package com.cdac.enrollmentstation.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractorCardInfo {
    String contractorId;
    String contractorName;
    String contractId; // gets updated on contract selection on ContractController
    String cardChipSerialNo;
    int cardHandle;
}
