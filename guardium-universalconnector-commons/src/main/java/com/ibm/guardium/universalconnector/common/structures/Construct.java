//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.commons.structures;

import java.util.ArrayList;

public class Construct {
    public ArrayList<Sentence> sentences = new ArrayList<>();
    public String fullSql;
    public String redactedSensitiveDataSql;

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(ArrayList<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getFullSql() {
        return fullSql;
    }

    public void setFullSql(String fullSql) {
        this.fullSql = fullSql;
    }

    public String getRedactedSensitiveDataSql() {
        return redactedSensitiveDataSql;
    }

    public void setRedactedSensitiveDataSql(String redactedSensitiveDataSql) {
        this.redactedSensitiveDataSql = redactedSensitiveDataSql;
    }
}