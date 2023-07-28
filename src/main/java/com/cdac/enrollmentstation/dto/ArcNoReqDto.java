package com.cdac.enrollmentstation.dto;
/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcNoReqDto {
    @JsonProperty("ARCNo")
    String arcNo;
}
