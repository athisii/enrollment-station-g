/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RealScan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author Gandhimathi
 */
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FP {
    /**
     * Pojo For Fingerprint Image Position
     * and Template.
     */
    @JsonProperty("Position")
    String position;

    @JsonProperty("Image")
    String image;

    @JsonProperty("Template")
    String template;

}
