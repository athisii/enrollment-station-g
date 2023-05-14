package com.cdac.enrollmentstation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Labour {
    @JsonProperty("DynamicFile")
    List<DynamicFileList> dynamicFileList;

    @JsonProperty("DefaultValidityFile")
    List<DefaultValidityFileList> defaultValidityFileList;

    @JsonProperty("AccessFile")
    List<AccessFileList> accessFileList;

    @JsonProperty("SignFile1")
    String signFile1;

    @JsonProperty("SignFile3")
    String signFile3;

    @JsonProperty("FPs")
    List<LabourFP> fps;

    @JsonProperty("IRIS1")
    String iris1;

    @JsonProperty("IRIS2")
    String iris2;

    @JsonProperty("Photo")
    String photo;

}
