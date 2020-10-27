//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.common.structures;

public class Record {

    /**
     * mandatory field - uniquely identifies sessionid
     */
    private String sessionId;

    /**
     * optional field - name of the database (db scheme)
     */
    private String dbName;

    /**
     * optional field - name of application user
     */
    private String appUserName;

    /**
     *  mandatory field - time of the event, in ms
     */
    private Time   time;

    /**
     * mandatory field - object that contains session related details
     */
    private SessionLocator  sessionLocator;

    /**
     * mandatory field - object that contains client connection details
     */
    private Accessor        accessor;

    /**
     * one of fields [data/ exception] must appear in the record
     * otherwise the record considered invalid
     */

    /**
     * mandatory field (one of fields [data/ exception] must appear in the record)
     * object that contains the details of an actual activity performed
     */
    private Data            data;

    /**
     * mandatory field (one of fields [data/ exception] must appear in the record)
     * object that contains session related details
     */
    private ExceptionRecord exception;

    public boolean isException(){
        return  (this.exception != null);
    }
    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
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

    public ExceptionRecord getException() {
        return exception;
    }

    public void setException(ExceptionRecord exception) {
        this.exception = exception;
    }
}
