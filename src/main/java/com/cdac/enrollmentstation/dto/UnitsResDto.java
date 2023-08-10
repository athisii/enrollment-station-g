package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author root
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnitsResDto {
    int errorCode;
    String desc;
    List<Unit> units;
}
