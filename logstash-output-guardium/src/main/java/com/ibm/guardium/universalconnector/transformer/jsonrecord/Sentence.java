package com.ibm.guardium.universalconnector.transformer.jsonrecord;

import java.util.ArrayList;

public class Sentence {
    private String verb;
    private ArrayList<SentenceObject> objects = new ArrayList<>();
    private ArrayList<Sentence> descendants = new ArrayList<>(); // TODO: implement?
    private ArrayList<String> fields = new ArrayList<>(); // TODO: implement? {key, value} objects

    public String getVerb() { return verb; }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public ArrayList<SentenceObject> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<SentenceObject> objects) {
        this.objects = objects;
    }

    public ArrayList<Sentence> getDescendants() {
        return descendants;
    }

    public void setDescendants(ArrayList<Sentence> descendants) {
        this.descendants = descendants;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }
}
