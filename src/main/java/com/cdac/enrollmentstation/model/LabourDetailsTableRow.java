package com.cdac.enrollmentstation.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabourDetailsTableRow {
    String labourId;
    String labourName;
    String dateOfBirth;
    String photo;
    String strStatus;
}
