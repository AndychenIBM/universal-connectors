package com.ibm.guardium.universalconnector.transmitter;

import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.status.StatusWriter;

import java.net.UnknownHostException;

public interface RecordTransmitter extends Runnable {

    void setup(ConnectionConfig config) throws UnknownHostException;
    boolean isStatusOpen();
    void stopRun();
    void setStatusWriter(StatusWriter statusWriter) ;
}
