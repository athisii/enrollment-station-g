package com.cdac.enrollmentstation.dto;
/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OnboardingReqDto {
    String pno;
    String hardwareType;
}
