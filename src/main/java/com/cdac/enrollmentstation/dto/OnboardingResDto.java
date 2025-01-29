package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author athisii
 * @version 1.0
 * @since 1/29/25
 */

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OnboardingResDto {
    String unitCode;
    String unitName;
    List<String> deviceSerialNos;
    int errorCode;
    String desc;
}
