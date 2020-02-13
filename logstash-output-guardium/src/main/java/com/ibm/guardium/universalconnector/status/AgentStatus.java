package com.ibm.guardium.universalconnector.status;


public class AgentStatus {

    private String statusId;
    private String agentId;
    private String count;
    private String status;
    private String comment;
    private String creationTimestamp;
    private Boolean isStatusExistedBeforeConsumerStart;

    public AgentStatus() { isStatusExistedBeforeConsumerStart = false; }

    public Boolean getIsStatusExistedBeforeConsumerStart() { return isStatusExistedBeforeConsumerStart; }
    public void setIsStatusExistedBeforeConsumerStart(Boolean isStatusExistedBeforeConsumerStart) {
        this.isStatusExistedBeforeConsumerStart = isStatusExistedBeforeConsumerStart;
    }

    public String getStatusId() { return statusId; }
    public void setStatusId(String statusId) { this.statusId = statusId; }
    public AgentStatus withStatusId(String statusId) {
        setStatusId(statusId);
        return this;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public AgentStatus withAgentId(String agentId) {
        setAgentId(agentId);
        return this;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public AgentStatus withStatus(String status) {
        setStatus(status);
        return this;
    }
    public Boolean isSameStatus(String newStatus) {
        return status.equals(newStatus);
    }

    public String getCount() { return count; }
    public void setCount(String count) { this.count = count; }
    public AgentStatus withCount(String count) {
        setCount(count);
        return this;
    }
    public void incrementCount() {
        count = String.valueOf(1 + Integer.parseInt(count));
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public AgentStatus withComment(String comment) {
        setComment(comment);
        return this;
    }
    public Boolean isSameComment(String newComment) {
        return comment.equals(newComment);
    }

    public String getcreationTimestamp() { return creationTimestamp; }
    public void setLastTimestamp(String creationTimestamp) { this.creationTimestamp = creationTimestamp; }
    public AgentStatus withcreationTimestamp(String creationTimestamp) {
        setLastTimestamp(creationTimestamp);
        return this;
    }

    @Override
    public String toString() {
        return "AgentStatus{" +
                "statusId='" + statusId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", count='" + count + '\'' +
                ", status='" + status + '\'' +
                ", comment='" + comment + '\'' +
                ", lastTimestamp='" + creationTimestamp + '\'' +
                '}';
    }
}
