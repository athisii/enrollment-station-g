package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author K. Karthikeyan
 */

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetailsResDto {
    int errorCode;
    String desc;
    List<ArcDetail> arcDetails;
}