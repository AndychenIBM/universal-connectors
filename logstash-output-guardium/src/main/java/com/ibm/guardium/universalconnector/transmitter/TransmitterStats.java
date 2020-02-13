package com.ibm.guardium.universalconnector.transmitter;

public class TransmitterStats {
    private long lastMsgCreateTime = 0; // epoc timestamp of msg when entered into stream.
    private long lastReceiveTime = 0;
    private long bytesSent = 0;
    private long errors = 0;
    private long reconnects = 0;
    private long msgsSent = 0;
    private long incomingRecords = 0;
    private long recordsInQ = 0;

    public void setLastMsgCreateTime(long lastMsgCreateTime) {
        this.lastMsgCreateTime = lastMsgCreateTime;
    }

    public long getLastMsgCreateTime() {
        return lastMsgCreateTime;
    }

    public long getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(long lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public void incrementBytesSent(long bSent) {
        this.bytesSent += bSent;
    }

    public long getErrors() {
        return errors;
    }

    public void incrementErrors() {
        this.errors++;
    }

    public long getReconnects() {
        return reconnects;
    }

    public void incrementReconnects() {
        this.reconnects++;
    }

    public long getMsgsSent() {
        return msgsSent;
    }

    public void incrementMsgsSent() {
        this.msgsSent++;
    }

    public long getIncomingRecords() {
        return incomingRecords;
    }

    public void incrementIncomingRecords() {
        incomingRecords++;
    }

    public void setIncomingRecords(long incomingRecords) {
        this.incomingRecords = incomingRecords;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setErrors(long errors) {
        this.errors = errors;
    }

    public void setReconnects(long reconnects) {
        this.reconnects = reconnects;
    }

    public void setMsgsSent(long msgsSent) {
        this.msgsSent = msgsSent;
    }

    public long getRecordsInQ() {
        return recordsInQ;
    }

    public void setRecordsInQ(long recordsInQ) {
        this.recordsInQ = recordsInQ;
    }

     public void copyTo(TransmitterStats b) {
         b.setLastMsgCreateTime(lastMsgCreateTime);
         b.setLastReceiveTime(lastReceiveTime);
         b.setBytesSent(bytesSent);
         b.setErrors(errors);
         b.setReconnects(reconnects);
         b.setMsgsSent(msgsSent);
         b.setIncomingRecords(incomingRecords);
         b.setRecordsInQ(recordsInQ);
    }

    @Override
    public String toString() {
        return "{\"TransmitterStats\":{"
                + "\"lastMsgCreateTime\":\"" + lastMsgCreateTime + "\""
                + ", \"lastReceiveTime\":\"" + lastReceiveTime + "\""
                + ", \"bytesSent\":\"" + bytesSent + "\""
                + ", \"errors\":\"" + errors + "\""
                + ", \"reconnects\":\"" + reconnects + "\""
                + ", \"msgsSent\":\"" + msgsSent + "\""
                + ", \"incomingRecords\":\"" + incomingRecords + "\""
                + ", \"recordsInQ\":\"" + recordsInQ + "\""
                + "}}";
    }
}
