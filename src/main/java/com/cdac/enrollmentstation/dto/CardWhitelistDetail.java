package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 * Created on 18/05/23
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardWhitelistDetail {
    @JsonProperty("pno")
    String pNo;
    @JsonProperty("cardNo")
    String cardNo;
    @JsonProperty("roleId")
    String roleId;
    @JsonProperty("roleName")
    String roleName;
}
