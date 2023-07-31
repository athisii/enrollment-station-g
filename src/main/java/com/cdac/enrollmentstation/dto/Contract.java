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
public class Contract {
    @JsonProperty("contractId")
    String contractId;
    @JsonProperty("contractValidFrom")
    String contractValidFrom;
    @JsonProperty("contractValidUpto")
    String contractValidUpto;
}
