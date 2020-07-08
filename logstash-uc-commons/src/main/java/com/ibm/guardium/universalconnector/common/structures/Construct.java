package com.ibm.guardium.universalconnector.common.structures;

import java.util.ArrayList;

public class Construct {
    public ArrayList<Sentence> sentences = new ArrayList<>();
    public String full_sql;
    public String original_sql;

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getFull_sql() {
        return full_sql;
    }

    public void setFull_sql(String full_sql) {
        this.full_sql = full_sql;
    }

    public String getOriginal_sql() {
        return original_sql;
    }

    public void setOriginal_sql(String original_sql) {
        this.original_sql = original_sql;
    }
}