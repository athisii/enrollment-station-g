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
public class Fp {
    String position;
    String image;
    String template;

}
