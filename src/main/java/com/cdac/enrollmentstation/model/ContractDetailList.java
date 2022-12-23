package com.cdac.enrollmentstation.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"ContractIDList", "ErrorCode", "Desc"})
public class ContractDetailList {
    @JsonProperty("ErrorCode")
    String errorCode;

    @JsonProperty("Desc")
    String desc;

    @JsonProperty("ContractIDList")
    Set<ContractDetail> contractDetailSet = new HashSet<>();

}
