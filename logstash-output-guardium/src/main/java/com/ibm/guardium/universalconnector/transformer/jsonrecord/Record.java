package com.ibm.guardium.universalconnector.transformer.jsonrecord;

public class Record {

    private String sessionId;
    private String dbName;
    private String appUserName;
    private long timeInMs;

    private SessionLocator  sessionLocator;
    private Accessor        accessor;
    private Data            data;
    private ExceptionRecord exception;

    public boolean isException(){
        return  (this.exception != null);
    }
    public long getTimeInMs() {
        return timeInMs;
    }

    public void setTimeInMs(long timeInMs) {
        this.timeInMs = timeInMs;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public SessionLocator getSessionLocator() {
        return sessionLocator;
    }

    public void setSessionLocator(SessionLocator sessionLocator) {
        this.sessionLocator = sessionLocator;
    }

    public Accessor getAccessor() {
        return accessor;
    }

    public void setAccessor(Accessor accessor) {
        this.accessor = accessor;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getAppUserName() {
        return appUserName;
    }

    public void setAppUserName(String appUserName) {
        this.appUserName = appUserName;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public ExceptionRecord getException() {
        return exception;
    }

    public void setException(ExceptionRecord exception) {
        this.exception = exception;
    }
}
