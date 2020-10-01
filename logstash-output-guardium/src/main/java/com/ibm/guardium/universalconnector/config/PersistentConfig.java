package com.ibm.guardium.universalconnector.config;

import java.util.Collection;

public class PersistentConfig {

    private UCConfig ucConfig;
    private SnifferConfig snifferConfig;
    private Collection<DatabaseDetails> dbs;

    public UCConfig getUcConfig() {
        return ucConfig;
    }

    public void setUcConfig(UCConfig ucConfig) {
        this.ucConfig = ucConfig;
    }

    public SnifferConfig getSnifferConfig() {
        return snifferConfig;
    }

    public void setSnifferConfig(SnifferConfig snifferConfig) {
        this.snifferConfig = snifferConfig;
    }

    public Collection<DatabaseDetails> getDbs() {
        return dbs;
    }

    public void setDbs(Collection<DatabaseDetails> dbs) {
        this.dbs = dbs;
    }
}
