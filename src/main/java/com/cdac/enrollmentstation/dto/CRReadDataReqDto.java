package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRReadDataReqDto {
    @JsonProperty("handle")
    int handle;
    @JsonProperty("whichdata")
    byte whichData;
    @JsonProperty("offset")
    int offset;
    @JsonProperty("reqlen")
    int reqLen;
}
