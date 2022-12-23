package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

//@JsonRootName("ContractIDList")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractIDNew {
    @JsonProperty("contractId")
    String contractId;

    @JsonProperty("contractName")
    String contractName;

    @JsonProperty("workOrderNo")
    String workOrderNo;
}
