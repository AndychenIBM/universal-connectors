package com.ibm.guardium.universalconnector.status;

public interface StatusWriter {
    public void updateStatus(String status, String comment);
    public void init() throws Exception;
}
