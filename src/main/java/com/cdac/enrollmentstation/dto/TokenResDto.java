package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResDto {
    @JsonProperty("ErrorCode")
    String errorCode;

    @JsonProperty("Desc")
    String desc;

    /**
     * @author root
     */
    @Getter
    @Setter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TokenReqDto {
        @JsonProperty("UniqueNo")
        String uniqueNo;

        @JsonProperty("EnrollmentStationID")
        String enrollmentStationID;

        @JsonProperty("CardCSN")
        String cardCSN;

        @JsonProperty("ContractorID")
        String contractorID;

        @JsonProperty("ContractorCSN")
        String contractorCSN;

        @JsonProperty("TokenIssuanceDate")
        String tokenIssuanceDate;

        @JsonProperty("ContractID")
        String contractID;

        @JsonProperty("EnrollmentStationUnitID")
        String enrollmentStationUnitID;

        @JsonProperty("TokenID")
        String tokenID;

        @JsonProperty("VerifyFPSerialNo")
        String verifyFPSerialNo;
    }
}