package com.ibm.guardium.universalconnector.config;

public class UCConfig {
    private String version;
    private String connectorId;
    private String connectorIp = "";
    private String configurationFetchType;
    private String configurationFetchFileName;
    private String statusWriterType;

    public UCConfig(){
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getConfigurationFetchType() {
        return configurationFetchType;
    }

    public void setConfigurationFetchType(String configurationFetchType) {
        this.configurationFetchType = configurationFetchType;
    }

    public String getConfigurationFetchFileName() {
        return configurationFetchFileName;
    }

    public void setConfigurationFetchFileName(String configurationFetchFileName) {
        this.configurationFetchFileName = configurationFetchFileName;
    }

    public String getStatusWriterType() {
        return statusWriterType;
    }

    public void setStatusWriterType(String statusWriterType) {
        this.statusWriterType = statusWriterType;
    }

    public String getConnectorIp() {
        return connectorIp;
    }

    public void setConnectorIp(String connectorIp) {
        this.connectorIp = connectorIp;
    }
}
