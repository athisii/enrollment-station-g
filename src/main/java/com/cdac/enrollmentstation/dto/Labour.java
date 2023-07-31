package com.cdac.enrollmentstation.dto;

import com.cdac.enrollmentstation.dto.DefaultValidityFile;
import com.cdac.enrollmentstation.dto.DynamicFile;
import com.cdac.enrollmentstation.dto.LabourFp;
import com.cdac.enrollmentstation.dto.SpecialAccessFile;
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
    DynamicFile dynamicFile;

    @JsonProperty("DefaultValidityFile")
    DefaultValidityFile defaultValidityFile;

    @JsonProperty("AccessFile")
    SpecialAccessFile specialAccessFile;

    @JsonProperty("SignFile1")
    String signFile1;

    @JsonProperty("SignFile3")
    String signFile3;

    @JsonProperty("FPs")
    List<LabourFp> fps;

    @JsonProperty("IRIS1")
    String iris1;

    @JsonProperty("IRIS2")
    String iris2;

    @JsonProperty("Photo")
    String photo;

}
