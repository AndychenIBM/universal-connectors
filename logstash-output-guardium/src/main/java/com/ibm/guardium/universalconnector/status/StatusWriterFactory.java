package com.ibm.guardium.universalconnector.status;


import com.ibm.guardium.universalconnector.config.UCConfig;

public class StatusWriterFactory {
    public StatusWriter Build(UCConfig ucConfig){
        if ("log".equalsIgnoreCase(ucConfig.getStatusWriterType())) {
            return new LogStatusWriter(ucConfig.getConnectorId(), ucConfig.getConnectorId());
        }
        throw new StatusException("Unknown statusWriterType, , check agent.properties, agent.config.statusWriterType");

    }
}
