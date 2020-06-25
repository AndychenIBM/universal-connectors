package com.ibm.guardium.s3.connector.structures;

public class Data {
    private Construct construct;
    private int timestamp; // limit warning: use new Date((long) unixTime * 1000), to revert to Date, otherwise, int max will be reached.
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

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }



}
