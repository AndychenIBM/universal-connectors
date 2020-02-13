package com.ibm.guardium.universalconnector.transformer.jsonrecord;

public class Data {
    private Construct construct;
    private int timestamp;
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

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }



}
