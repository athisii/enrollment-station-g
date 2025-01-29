package com.cdac.enrollmentstation.dto;

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
public class UserReqDto {
    String pno; // 0930065B
    String deviceSerialNo;
    String hardwareType;
    String unitCode;
}
