package com.ibm.guardium.universalconnector.transformer.jsonrecord;

public class ExceptionRecord {
    /*
    EXCEPTION_TYPE_ID
    SESSION_ID
    session
    DESCRIPTION
    DB_PROTOCOL
    SQL_STRING
    DB_USER
    TIMESTAMP
     */
    private String exceptionTypeId;
    private String description;
//    private String dbProtocol; //pop by accessor
    private String sqlString;
    private String timestamp;
//{\"exceptionTypeId\":\"SQL_ERROR\",\"description\":\"parse error\",\"sqlString\":\"select from sales\",\"timestamp\":\"1588573299000\"}
//{"exceptionTypeId":"SQL_ERROR","description":"parse error","sqlString":"select from sales","timestamp":"1588573299000"}
    public String getExceptionTypeId() {
        return exceptionTypeId;
    }

    public void setExceptionTypeId(String exceptionTypeId) {
        this.exceptionTypeId = exceptionTypeId;
    }

    public String getDescription() {
        return description;
    }

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
