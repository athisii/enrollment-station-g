package com.cdac.enrollmentstation.exception;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */

// Generic Exception Type to be used by all classes if needed to throw an exception.
public class GenericException extends RuntimeException {
    public GenericException(String message) {
        super(message);
    }

}
