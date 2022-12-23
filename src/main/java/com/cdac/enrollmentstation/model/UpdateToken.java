/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author root
 */
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateToken {
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
