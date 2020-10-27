//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.common.structures;

import java.util.ArrayList;

public class Sentence {
    private String verb;
    private ArrayList<SentenceObject> objects = new ArrayList<>();
    private ArrayList<Sentence> descendants = new ArrayList<>(); // TODO: implement?
    private ArrayList<String> fields = new ArrayList<>(); // TODO: implement? {key, value} objects

    public Sentence(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }

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