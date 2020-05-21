package com.ibm.guardium.connector.structures;

/**
 * Prepares an exception object to passed instead of a data object.
 * 
 * Make sure that Session ID, session, Server type, DB protocol, and DB user 
 * are also set in their natural place (Record.accessor, Record.session, etc). 
 */
public class ExceptionRecord {
    private String exceptionTypeId;
    private String description;
    private String sqlString;
    private String timestamp;
  
    public String getExceptionTypeId() {
        return exceptionTypeId;
    }

    /**
     * Set Exception type id, which acts as an error category in Guardium reports. 
     * @param exceptionTypeId - A constant known to Guardium, as LOGIN_FAILED or SQL_ERROR
     */
    public void setExceptionTypeId(String exceptionTypeId) {
        this.exceptionTypeId = exceptionTypeId;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Populates either (a) a description string or (b) an error code Guardium is familiar with.
     * 
     * You have 2 options: 
     * (A) Enter a string description of your choice, like "Unauthorized operation (13)"
     * (B) Enter just the error code, if you want Guardium to match to its familiar error code. 
     * Option B is not suitable for DBs that Guardium is not supporting using S-TAPs.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
