package com.ibm.guardium.connector.structures;

import java.util.ArrayList;

public class Sentence {
    public String verb;
    public ArrayList<SentenceObject> objects = new ArrayList<>();
    public ArrayList<Sentence> descendants = new ArrayList<>(); // TODO: implement?
    public ArrayList<String> fields = new ArrayList<>(); // TODO: implement? {key, value} objects

    public Sentence(String verb) {
        this.verb = verb;
    }
}