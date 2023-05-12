package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRWaitForConnectResDto {
    @JsonProperty("retval")
    int retVal;

    @JsonProperty("handle")
    int handle;

    @JsonProperty("csn")
    String csn;

    @JsonProperty("csnLength")
    int csnLength;

}
