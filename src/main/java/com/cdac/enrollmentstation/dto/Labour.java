package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Labour {
    DynamicFile dynamicFile;
    DefaultValidityFile defaultValidityFile;
    AccessFile accessFile;
    String signFile1;
    String signFile3;
    List<LabourFp> fps;
    String iris1;
    String iris2;
    String photo;
}
