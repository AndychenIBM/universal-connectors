package com.ibm.guardium.universalconnector.config;

import java.util.Objects;

public class SnifferConfig {

    private String ip;
    private int port;

    public SnifferConfig(String snifferHost, int snifferPort, boolean isSSL) {
        this.ip = snifferHost;
        this.port = snifferPort;
    }

    public String getId(){
        return ip;
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

    @Override
    public String toString() {
        return "SnifferConfig{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnifferConfig that = (SnifferConfig) o;
        return getPort() == that.getPort() &&
                Objects.equals(getIp(), that.getIp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIp(), getPort());
    }
}
