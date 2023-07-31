package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * @author athisii, CDAC
 * Created on 13/05/23
 */

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractResDto {
    @JsonProperty("ErrorCode")
    String errorCode;

    @JsonProperty("Desc")
    String desc;

    @JsonProperty("ContractIDList")
    Set<Contract> contracts;

}