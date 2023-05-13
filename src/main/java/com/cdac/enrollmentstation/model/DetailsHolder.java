package com.cdac.enrollmentstation.model;

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
public class DetailsHolder {
    List<ContractDetail> contractDetailList;
    ContractInfo contractDetail;
    //TODO should contain only List<Labour>
    LabourListDetails labourListDetail;
    private static final DetailsHolder detail = new DetailsHolder();

    public static DetailsHolder getDetails() {
        return detail;
    }

}
