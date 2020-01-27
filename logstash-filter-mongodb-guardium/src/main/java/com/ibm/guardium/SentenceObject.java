package com.ibm.guardium;

public class SentenceObject {
    public String name; 
    public String type = "collection"; // in mongo
    public String[] fields = new String[]{};
    public String schema = "";

    SentenceObject(String name) {
        this.name = name;
    }
}