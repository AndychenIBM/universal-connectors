//
// Copyright 2020- IBM Inc. All rights reserved
// SPDX-License-Identifier: Apache2.0
//
package com.ibm.guardium.universalconnector.common.structures;

public class Data {
    private Construct construct;
    /**
     * this field is only required if guardium should parse sql (instead of using construct object)
     */
    private String originalSqlCommand;

    public void setOriginalSqlCommand(String originalSqlCommand) {
        this.originalSqlCommand = originalSqlCommand;
    }

    public Construct getConstruct() {
        return construct;
    }

    public void setConstruct(Construct construct) {
        this.construct = construct;
    }

    public String getOriginalSqlCommand() {
        return originalSqlCommand;
    }



}
