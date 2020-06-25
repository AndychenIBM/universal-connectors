package com.ibm.guardium.s3.connector.structures;

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
}