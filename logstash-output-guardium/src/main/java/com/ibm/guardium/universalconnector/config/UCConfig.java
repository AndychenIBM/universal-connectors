package com.ibm.guardium.universalconnector.config;

import java.util.Objects;

public class UCConfig {
    private String version;
    private String containerUniqueId;
    private String connectorId;
    private String connectorIp = "";
    private String configurationFetchType;
    private String configurationFetchFileName;
    private String statusWriterType;
    private Integer snifferConnectionsLimit;

    public UCConfig(){
    }

    public String getContainerUniqueId() {
        return containerUniqueId;
    }

    public void setContainerUniqueId(String containerUniqueId) {
        this.containerUniqueId = containerUniqueId;
    }

    public String getId(){
        return connectorId;
    }

    public Integer getSnifferConnectionsLimit() {
        return snifferConnectionsLimit;
    }

    public void setSnifferConnectionsLimit(Integer snifferConnectionsLimit) {
        this.snifferConnectionsLimit = snifferConnectionsLimit;
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

    @Override
    public String toString() {
        return "UCConfig{" +
                "version='" + version + '\'' +
                ", connectorId='" + connectorId + '\'' +
                ", connectorIp='" + connectorIp + '\'' +
                ", configurationFetchType='" + configurationFetchType + '\'' +
                ", configurationFetchFileName='" + configurationFetchFileName + '\'' +
                ", statusWriterType='" + statusWriterType + '\'' +
                ", snifferConnectionsLimit =" + snifferConnectionsLimit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UCConfig ucConfig = (UCConfig) o;
        return Objects.equals(getVersion(), ucConfig.getVersion()) &&
                Objects.equals(getConnectorId(), ucConfig.getConnectorId()) &&
                Objects.equals(getConnectorIp(), ucConfig.getConnectorIp()) &&
                Objects.equals(getConfigurationFetchType(), ucConfig.getConfigurationFetchType()) &&
                Objects.equals(getConfigurationFetchFileName(), ucConfig.getConfigurationFetchFileName()) &&
                Objects.equals(getStatusWriterType(), ucConfig.getStatusWriterType()) &&
                Objects.equals(getSnifferConnectionsLimit(), ucConfig.getSnifferConnectionsLimit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersion(), getConnectorId(), getConnectorIp(), getConfigurationFetchType(), getConfigurationFetchFileName(), getStatusWriterType(), getSnifferConnectionsLimit());
    }
}
