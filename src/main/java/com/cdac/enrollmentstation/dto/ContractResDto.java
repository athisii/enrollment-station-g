package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * @author athisii, CDAC
 * Created on 13/05/23
 */

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractResDto {
    int errorCode;
    String desc;
    @JsonProperty("contractIdList")
    Set<Contract> contracts;

}