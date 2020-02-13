package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.universalconnector.config.SnifferConfig;

import java.io.FileNotFoundException;
import java.util.List;

public interface ConfigurationFetcher {
    public List<SnifferConfig> fetch() throws FileNotFoundException;
}
