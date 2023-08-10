package com.cdac.enrollmentstation.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccessFile {
    String unitCode;
    String zoneId;
    String workingHourCode;
    String fromDate;
    String toDate;

}
