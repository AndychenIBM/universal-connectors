package com.ibm.guardium.universalconnector.config;

import com.ibm.guardium.proto.datasource.Datasource;

public class DatabaseDetails {

    public static final String DELIMITER = "_";
    private String dbName;
    private String dbHost;
    private int    dbPort;

    public String getId(){
        String id = /*dbName+DELIMITER+ */dbHost +DELIMITER+dbPort;
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

    public static DatabaseDetails buildFromMessage(Datasource.Session_start ss){
        //String serverIp = sl.getIsIpv6() ? sl.getServerIpv6() : ""+sl.getServerIp();
        DatabaseDetails dd = new DatabaseDetails();
        dd.setDbName(ss.getDbName());
        dd.setDbPort(ss.getSessionLocator().getServerPort());
        dd.setDbHost(ss.getAccessor().getServerHostname());
        return dd;
    }

    @Override
    public String toString() {
        return "DatabaseDetails{" +
                "dbName='" + dbName + '\'' +
                ", dbHost='" + dbHost + '\'' +
                ", dbPort=" + dbPort +
                '}';
    }
}
