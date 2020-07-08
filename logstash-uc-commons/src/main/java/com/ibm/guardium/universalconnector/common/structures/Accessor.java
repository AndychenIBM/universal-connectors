package com.ibm.guardium.universalconnector.common.structures;

public class Accessor {
    // TYPE
    public static final String TYPE_CONSTRUCT_STRING = "CONSTRUCT"; // Signals Guardium not to parse
    private static final String TYPE_TEXT_STRING = "TEXT";  // Signals Guardium to parse (supported DB languages only)
    
    public static final String LANGUAGE_FREE_TEXT_STRING = "FREE_TEXT"; // Used when no need to parse by Guardium Sniffer 

	private String dbUser;
    private String serverType;
    private String serverOs;
    private String clientOs;
    private String clientHostName;
    private String serverHostName;
    private String commProtocol;
    private String dbProtocol;
    private String dbProtocolVersion;
    private String osUser;
    private String sourceProgram;
    private String client_mac;
    private String serverDescription;
    private String serviceName;
    private String language;
    private String type;

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getServerOs() {
        return serverOs;
    }

    public void setServerOs(String serverOs) {
        this.serverOs = serverOs;
    }

    public String getClientOs() {
        return clientOs;
    }

    public void setClientOs(String clientOs) {
        this.clientOs = clientOs;
    }

    public String getClientHostName() {
        return clientHostName;
    }

    public void setClientHostName(String clientHostName) {
        this.clientHostName = clientHostName;
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public String getCommProtocol() {
        return commProtocol;
    }

    public void setCommProtocol(String commProtocol) {
        this.commProtocol = commProtocol;
    }

    public String getDbProtocol() {
        return dbProtocol;
    }

    public void setDbProtocol(String dbProtocol) {
        this.dbProtocol = dbProtocol;
    }

    public String getDbProtocolVersion() {
        return dbProtocolVersion;
    }

    public void setDbProtocolVersion(String dbProtocolVersion) {
        this.dbProtocolVersion = dbProtocolVersion;
    }

    public String getOsUser() {
        return osUser;
    }

    public void setOsUser(String osUser) {
        this.osUser = osUser;
    }

    public String getSourceProgram() {
        return sourceProgram;
    }

    public void setSourceProgram(String sourceProgram) {
        this.sourceProgram = sourceProgram;
    }

    public String getClient_mac() {
        return client_mac;
    }

    public void setClient_mac(String client_mac) {
        this.client_mac = client_mac;
    }

    public String getServerDescription() {
        return serverDescription;
    }

    public void setServerDescription(String serverDescription) {
        this.serverDescription = serverDescription;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
