package com.cdac.enrollmentstation.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;


/**
 * @author athisii, CDAC
 * Created on 20/12/22
 */
public class Singleton {
    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = JsonMapper.builder()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE) // to change property naming automatically
                    .build();
        }
        return objectMapper;
    }

    //Suppress default constructor for noninstantiability
    private Singleton() {
        throw new AssertionError("The Singleton methods should be accessed statically");
    }


}
