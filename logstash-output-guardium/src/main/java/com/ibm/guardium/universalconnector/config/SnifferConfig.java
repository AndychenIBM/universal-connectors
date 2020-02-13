package com.ibm.guardium.universalconnector.config;

public class SnifferConfig {

    private String ip;
    private int port;
    private boolean isSSL;

    public SnifferConfig(String snifferHost, int snifferPort, boolean isSSL) {
        this.ip = snifferHost;
        this.port = snifferPort;
        this.isSSL = isSSL;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSSL() {
        return isSSL;
    }

    public void setSSL(boolean SSL) {
        isSSL = SSL;
    }
}
