package com.ibm.guardium.connector.structures;

public class SentenceObject {
    public String name; 
    public String type = "collection"; // in mongo
    public String[] fields = new String[]{};
    public String schema = "";

    public SentenceObject(String name) {
        this.name = name;
    }
}