package com.ibm.guardium.universalconnector.transformer.jsonrecord;

public class Data {
    private Construct construct;
    private long timestamp; // milliseconds
    private String originalSqlCommand;
    private boolean useConstruct;

    public void setOriginalSqlCommand(String originalSqlCommand) {
        this.originalSqlCommand = originalSqlCommand;
    }

    public boolean isUseConstruct() {
        return useConstruct;
    }

    public void setUseConstruct(boolean useConstruct) {
        this.useConstruct = useConstruct;
    }

    public Construct getConstruct() {
        return construct;
    }

    public void setConstruct(Construct construct) {
        this.construct = construct;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp action, in DB, in milliseconds precision (long) 
     * @param timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }



}
