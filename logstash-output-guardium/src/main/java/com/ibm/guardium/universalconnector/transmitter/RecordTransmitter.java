package com.ibm.guardium.universalconnector.transmitter;

import com.ibm.guardium.universalconnector.config.SnifferConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;

import java.net.UnknownHostException;

public interface RecordTransmitter extends Runnable {

    void setup(UCConfig config, SnifferConfig snifferConfig) throws UnknownHostException;
    boolean isStatusOpen();
    void stopRun();
}
