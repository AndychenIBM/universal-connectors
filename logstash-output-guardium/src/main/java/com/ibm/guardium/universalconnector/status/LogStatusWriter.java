package com.ibm.guardium.universalconnector.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LogStatusWriter implements StatusWriter {
    private Logger log = LogManager.getLogger(LogStatusWriter.class);
    private AgentStatus currentActive = new AgentStatus().withStatus("").withComment("");
    private String agentId;
    private String tenantId;

    public LogStatusWriter(String agentId, String tenantId) {
        this.agentId = agentId;
        this.tenantId = tenantId;
    }

    private void logStatus(String status, String comment) {
        log.info("tenantId:" + tenantId + ", agentId:" + agentId + ", " + status + "," + comment);
        currentActive.setComment(comment);
        currentActive.setStatus(status);
    }

    private Boolean isNoChange(String status, String comment){
        return ((currentActive.isSameStatus(status)) && (currentActive.isSameComment(comment)));
    }

    public void updateStatus(String status, String comment){
        try {
            if (isNoChange(status, comment)) {
                return;
            }
            logStatus(status, comment);
        } catch (Exception e) {
            log.error("updateStatus failed", e);
        }
    }

    public void init(){
        log.info("LogStatusWriter started");
    };
}
