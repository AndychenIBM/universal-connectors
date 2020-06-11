package com.ibm.guardium.universalconnector.config;

import com.ibm.guardium.proto.datasource.Datasource;

public class DatabaseDetails {

    public static final String DELIMITER = ":";
    private String dbName;
    private String dbHost;
    private int    dbPort;
    private String dbType;

    public String getId(){
        String id = dbHost +DELIMITER+dbPort;
        return id;
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

    public static DatabaseDetails buildFromMessage(Datasource.Session_start ss){
        DatabaseDetails dd = new DatabaseDetails();
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
}
