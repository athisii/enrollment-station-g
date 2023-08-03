package com.cdac.enrollmentstation.exception;

public class NoReaderOrCardException extends RuntimeException {
    public NoReaderOrCardException(String message) {
        super(message);
    }
}
