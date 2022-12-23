package com.cdac.enrollmentstation.model;

import com.cdac.enrollmentstation.model.Labour;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName(value = "Labours")
public class LabourListDetails {
    @JsonProperty("ErrorCode")
    String errorCode;
    
    @JsonProperty("Desc")
    String desc;
    
    @JsonProperty("LabourList")
    List<Labour> labourList = new ArrayList<>();

}