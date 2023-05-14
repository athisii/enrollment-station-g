package com.cdac.enrollmentstation.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailsHolder {
    List<ContractDetail> contractDetailList; //not really required so far
    ContractorInfo contractorInfo; // sets by ContractorTokenIssuanceController and ContractController
    List<Labour> labours; // to used in single contract
    private static final DetailsHolder INSTANCE = new DetailsHolder();

    // Should not instantiate outside the class.
    private DetailsHolder() {
    }

    public static DetailsHolder getDetailsHolder() {
        return INSTANCE;
    }

}
