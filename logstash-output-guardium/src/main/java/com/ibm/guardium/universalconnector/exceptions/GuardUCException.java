package com.ibm.guardium.universalconnector.exceptions;


public class GuardUCException extends RuntimeException {
    public GuardUCException(String message) {
        super(message);
    }
    public GuardUCException(String message, Throwable e) {
        super(message, e);
    }
    public GuardUCException(Throwable e) {
        super(e);
    }
}
