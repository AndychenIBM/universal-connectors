package com.ibm.guardium.universalconnector.transformer.jsonrecord;

public class SentenceObject {
    private String name;
    private String type = "collection"; // in mongo
    private String[] fields = new String[]{};
    private String schema = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
