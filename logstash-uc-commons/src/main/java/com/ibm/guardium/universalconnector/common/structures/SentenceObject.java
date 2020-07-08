package com.ibm.guardium.universalconnector.common.structures;

public class SentenceObject {
    public String name; 
    public String type = "collection"; // in mongo
    public String[] fields = new String[]{};
    public String schema = "";

    public SentenceObject(String name) {
        this.name = name;
    }

    public SentenceObject(String name, String schema) {
        this.name = name;
        this.schema = schema;
    }

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