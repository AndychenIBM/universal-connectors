package com.ibm.guardium.universalconnector.config;

public class ConnectionConfig {

    public static final String ID_DELIMITER="_";
    public static final String PREFIX = "UC";
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
        sb.append(PREFIX);

        if (ucConfig!=null){
            sb.append(ucConfig.getId());
        }
//        if (snifferConfig!=null){
//            if (sb.length()>0){
//                sb.append(ID_DELIMITER).append(snifferConfig.getId());
//            } else {
//                sb.append(snifferConfig.getId());
//            }
//        }
        if (databaseDetails !=null){
            if (sb.length()>0){
                sb.append(ID_DELIMITER).append(databaseDetails.getId());
            } else {
                sb.append(databaseDetails.getId());
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


}
