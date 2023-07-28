package com.cdac.enrollmentstation.exception;

/**
 * @author athisii, CDAC
 * Created on 17/12/22
 */

// Generic Exception Type to be used by all classes if needed to throw an exception.
public class ConnectionTimeoutException extends RuntimeException {
    public ConnectionTimeoutException(String message) {
        super(message);
    }

}
