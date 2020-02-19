package com.ibm.guardium.connector.structures;

public class SessionLocator {

    private String clientIp;
    private int clientPort;
    private String serverIp;
    private int    serverPort;
    private boolean isIpv6;
    private String clientIpv6;
    private String serverIpv6;

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isIpv6() {
        return isIpv6;
    }

    public void setIpv6(boolean ipv6) {
        isIpv6 = ipv6;
    }

    public String getClientIpv6() {
        return clientIpv6;
    }

    public void setClientIpv6(String clientIpv6) {
        this.clientIpv6 = clientIpv6;
    }

    public String getServerIpv6() {
        return serverIpv6;
    }

    public void setServerIpv6(String serverIpv6) {
        this.serverIpv6 = serverIpv6;
    }
}
