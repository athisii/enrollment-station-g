package com.cdac.enrollmentstation.dto;

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
public class TokenReqDto {
    @JsonProperty("UniqueNo")
    String uniqueNo;

    @JsonProperty("EnrollmentStationID")
    String enrollmentStationId;

    @JsonProperty("CardCSN")
    String cardCsn;

    @JsonProperty("ContractorID")
    String contractorId;

    @JsonProperty("ContractorCSN")
    String contractorCsn;

    @JsonProperty("TokenIssuanceDate")
    String tokenIssuanceDate;

    @JsonProperty("ContractID")
    String contractId;

    @JsonProperty("EnrollmentStationUnitID")
    String enrollmentStationUnitId;

    @JsonProperty("TokenID")
    String tokenId;

    @JsonProperty("VerifyFPSerialNo")
    String verifyFpSerialNo;
}