package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author K. Karthikeyan
 */

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetailsResDto {
    @JsonProperty("ErrorCode")
    int errorCode;

    @JsonProperty("Desc")
    String desc;

    @JsonProperty("arcDetails")
    List<ArcDetail> arcDetails;
}