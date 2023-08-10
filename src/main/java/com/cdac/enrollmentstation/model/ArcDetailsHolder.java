package com.cdac.enrollmentstation.model;

import com.cdac.enrollmentstation.dto.ArcDetail;
import com.cdac.enrollmentstation.dto.SaveEnrollmentDetail;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author HP
 */

@Getter
@Setter

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetailsHolder {
    ArcDetail arcDetail;
    SaveEnrollmentDetail saveEnrollmentDetail;
    private static final ArcDetailsHolder INSTANCE = new ArcDetailsHolder();

    // Should not instantiate outside the class.
    private ArcDetailsHolder() {
    }

    public static ArcDetailsHolder getArcDetailsHolder() {
        return INSTANCE;
    }

}


