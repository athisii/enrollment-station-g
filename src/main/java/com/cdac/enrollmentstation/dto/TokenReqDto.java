package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenReqDto {
    String uniqueNo;
    String enrollmentStationId;
    String cardCsn;
    String contractorId;
    String contractorCsn;
    String tokenIssuanceDate;
    String contractId;
    String enrollmentStationUnitId;
    String tokenId;
    String verifyFpSerialNo;
    String labourStatus;
    String tokenIssuanceStatus;
}