package com.ibm.guardium.connector.structures;

public class Record {

    private String sessionId;
    private String dbName;
    private String appUserName;
    private int    time;

    private SessionLocator  sessionLocator;
    private Accessor        accessor;
    private Data            data;

    public int getTime() {
        return time;
    }
    
    public void setTime(int time) {
        this.time = time;
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
}
