package com.cdac.enrollmentstation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author root
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    String caption;
    String value;
}
