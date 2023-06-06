package com.ibm.guardium.universalconnector.config;

import com.ibm.guardium.proto.datasource.Datasource;
import com.ibm.guardium.universalconnector.commons.structures.Record;

import java.util.Objects;

public class DatabaseDetails {

    public static final String DELIMITER = ":";
    private String connectorName;
    private String connectorId;
    private String dbName;
    private String dbHost;
    private int    dbPort;
    private String dbType;

    public String getId(){
        StringBuffer sb = new StringBuffer();
        if (connectorName!=null){
            sb.append(connectorName).append(DELIMITER);
        }
        if (connectorId!=null){
            sb.append(connectorId).append(DELIMITER);
        }
        sb.append(dbHost).append(DELIMITER).append(dbPort);
        return sb.toString();
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getConnectorName() { return connectorName; }

    public void setConnectorName(String connectorName) { this.connectorName = connectorName; }

    public String getConnectorId() { return connectorId; }

    public void setConnectorId(String connectorId) { this.connectorId = connectorId; }

    public static DatabaseDetails buildFromMessage(Datasource.Session_start ss, Record record){
        DatabaseDetails dd = new DatabaseDetails();
        dd.setConnectorName(record.getConnectorName());
        dd.setConnectorId(record.getConnectorId());
        dd.setDbName(ss.getDbName());
        dd.setDbPort(ss.getSessionLocator().getServerPort());
        dd.setDbHost(ss.getAccessor().getServerHostname());
        dd.setDbType(ss.getAccessor().getServerType());
        return dd;
    }

    @Override
    public String toString() {
        return "DatabaseDetails{" +
                "dbName='" + dbName + '\'' +
                ", dbHost='" + dbHost + '\'' +
                ", dbPort=" + dbPort +
                ", dbType=" + dbType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseDetails that = (DatabaseDetails) o;
        return getDbPort() == that.getDbPort() &&
                Objects.equals(getDbName(), that.getDbName()) &&
                Objects.equals(getDbHost(), that.getDbHost()) &&
                Objects.equals(getDbType(), that.getDbType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDbName(), getDbHost(), getDbPort(), getDbType());
    }
}
