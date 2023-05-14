package com.cdac.enrollmentstation.dto;

import com.cdac.enrollmentstation.model.Labour;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author athisii, CDAC
 * Created on 13/05/23
 */


@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabourResDto {
    @JsonProperty("ErrorCode")
    String errorCode;

    @JsonProperty("Desc")
    String desc;

    @JsonProperty("LabourList")
    List<Labour> labours;

}
