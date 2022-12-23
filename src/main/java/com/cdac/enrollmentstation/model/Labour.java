package com.cdac.enrollmentstation.model;

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
public class Labour {
    @JsonProperty("DynamicFile")
    List<DynamicFileList> dynamicFileList = new ArrayList<>();

    @JsonProperty("DefaultValidityFile")
    List<DefaultValidityFileList> defaultValidityFileList = new ArrayList<>();

    @JsonProperty("AccessFile")
    List<AccessFileList> accessFileList = new ArrayList<>();

    @JsonProperty("SignFile1")
    String signFile1;

    @JsonProperty("SignFile3")
    String signFile3;

    @JsonProperty("FPs")
    List<LabourFP> fPs = new ArrayList<>();

    @JsonProperty("IRIS1")
    String iRIS1;

    @JsonProperty("IRIS2")
    String iRIS2;

    @JsonProperty("Photo")
    String photo;

}
