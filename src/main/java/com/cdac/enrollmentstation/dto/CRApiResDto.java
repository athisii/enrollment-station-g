package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRApiResDto {
    @JsonProperty("retval")
    int retVal;
}
