package com.cdac.enrollmentstation.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Details {
    ContractDetailList tokenDetail;

    ContactDetail contractDetail;

    LabourListDetails labourListDetail;

    private static final Details detail = new Details();

    public static Details getdetails() {
        return detail;
    }

}
