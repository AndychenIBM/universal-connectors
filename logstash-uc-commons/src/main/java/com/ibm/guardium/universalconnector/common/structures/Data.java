package com.ibm.guardium.universalconnector.common.structures;

public class Data {
    private Construct construct;
    /**
     * this field is only required if guardium should parse sql (instead of using construct object)
     */
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

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }



}
