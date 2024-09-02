package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Labour {
    DynamicFile dynamicFile;
    String dynamicFileASN;
    DefaultValidityFile defaultValidityFile;
    String defaultValidityFileASN;
    AccessFile accessFile;
    String accessFileASN;
    String signFile1;
    String signFile3;
    String fingerPrintASN;
    List<LabourFp> fps;
    String iris1;
    String iris2;
    String photo;
}
