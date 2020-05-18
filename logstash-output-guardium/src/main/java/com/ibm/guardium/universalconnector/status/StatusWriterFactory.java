package com.ibm.guardium.universalconnector.status;


import com.ibm.guardium.universalconnector.config.ConnectionConfig;
import com.ibm.guardium.universalconnector.config.UCConfig;

public class StatusWriterFactory {
    public StatusWriter Build(ConnectionConfig config){
        if ("log".equalsIgnoreCase(config.getUcConfig().getStatusWriterType())) {
            return new LogStatusWriter(config.getId(), config.getId());
        }
        throw new StatusException("Unknown statusWriterType, , check agent.properties, agent.config.statusWriterType");

    }
}
