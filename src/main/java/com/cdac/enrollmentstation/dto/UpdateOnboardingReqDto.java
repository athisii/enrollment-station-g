package com.cdac.enrollmentstation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii
 * @version 1.0
 * @since 1/29/25
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOnboardingReqDto {
    String deviceSerialNo;
    String unitCode;
    /*
        1 - onboard
        2 - de-onboard
    */
    int type;
}
