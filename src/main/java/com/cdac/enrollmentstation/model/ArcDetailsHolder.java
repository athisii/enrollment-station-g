package com.cdac.enrollmentstation.model;

import com.cdac.enrollmentstation.dto.ArcDetails;
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
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArcDetailsHolder {
    ArcDetails arcDetails;
    SaveEnrollmentDetails saveEnrollmentDetails;
    private static final ArcDetailsHolder INSTANCE = new ArcDetailsHolder();

    // Should not instantiate outside the class.
    private ArcDetailsHolder() {
    }

    public static ArcDetailsHolder getArcDetailsHolder() {
        return INSTANCE;
    }

}


