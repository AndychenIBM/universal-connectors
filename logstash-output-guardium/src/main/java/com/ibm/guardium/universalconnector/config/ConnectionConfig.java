package com.ibm.guardium.universalconnector.config;

import java.util.Objects;

public class ConnectionConfig {

    public static final String ID_DELIMITER=":";
    public static final String UC_STR = "UC";
    private UCConfig ucConfig;
    private SnifferConfig snifferConfig;
    private DatabaseDetails databaseDetails;

    public ConnectionConfig(UCConfig ucConfig, SnifferConfig snifferConfig, DatabaseDetails databaseDetails) {
        this.ucConfig = ucConfig;
        this.snifferConfig = snifferConfig;
        this.databaseDetails = databaseDetails;
    }

    public String getId(){
        StringBuffer sb = new StringBuffer();
        if (databaseDetails !=null){
            sb.append(databaseDetails.getId());
        }
        if (ucConfig!=null){
            //sb.append(" ").append("(").append(databaseDetails.getDbType()).append(ID_DELIMITER).append(UC_STR).append(")");
            sb.append(ID_DELIMITER).append(UC_STR).append(ucConfig.getId());
            // fix for INS-12471 - sniffer limitation - need to have different connection id on each pod
            String containerId = ucConfig.getContainerUniqueId();
            if (containerId!=null && containerId.trim().length()>0){
                sb.append(ID_DELIMITER).append(containerId);
            }
        }
        return sb.toString();
    }

    public UCConfig getUcConfig() {
        return ucConfig;
    }

    public void setUcConfig(UCConfig ucConfig) {
        this.ucConfig = ucConfig;
    }

    public SnifferConfig getSnifferConfig() {
        return snifferConfig;
    }

    public void setSnifferConfig(SnifferConfig snifferConfig) {
        this.snifferConfig = snifferConfig;
    }

    public DatabaseDetails getDatabaseDetails() {
        return databaseDetails;
    }

    public void setDatabaseDetails(DatabaseDetails databaseDetails) {
        this.databaseDetails = databaseDetails;
    }

    @Override
    public String toString() {
        return "ConnectionConfig{" +
                "ucConfig=" + ucConfig +
                ", snifferConfig=" + snifferConfig +
                ", databaseDetails=" + databaseDetails +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionConfig that = (ConnectionConfig) o;
        return Objects.equals(getUcConfig(), that.getUcConfig()) &&
                Objects.equals(getSnifferConfig(), that.getSnifferConfig()) &&
                Objects.equals(getDatabaseDetails(), that.getDatabaseDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUcConfig(), getSnifferConfig(), getDatabaseDetails());
    }
}
