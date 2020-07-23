package com.ibm.guardium.universalconnector.exceptions;

public class GuardUCInvalidRecordException extends RuntimeException {
    public GuardUCInvalidRecordException(String message) {
        super(message);
    }
    public GuardUCInvalidRecordException(String message, Throwable e) {
        super(message, e);
    }
    public GuardUCInvalidRecordException(Throwable e) {
        super(e);
    }
}
