package com.cdac.enrollmentstation.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenDetailsHolder {
    ContractorCardInfo contractorCardInfo; // sets by ContractorTokenIssuanceController and ContractController
    private static final TokenDetailsHolder INSTANCE = new TokenDetailsHolder();

    // Should not instantiate outside the class.
    private TokenDetailsHolder() {
    }

    public static TokenDetailsHolder getDetailsHolder() {
        return INSTANCE;
    }

}
