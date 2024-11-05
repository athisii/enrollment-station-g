package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author athisii, CDAC
 * Created on 18/05/23
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardWhitelistResDto {
    @JsonProperty("cardWhitelistDetails")
    List<CardWhitelistDetail> cardWhitelistDetails;
    int errorCode;
    String desc;
}
