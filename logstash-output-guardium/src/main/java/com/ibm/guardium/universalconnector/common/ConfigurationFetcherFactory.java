package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.universalconnector.config.UCConfig;
import com.ibm.guardium.universalconnector.exceptions.GuardUCException;

public class ConfigurationFetcherFactory {
    public ConfigurationFetcher Build(UCConfig ucConfig){
        String FetchType = ucConfig.getConfigurationFetchType();
        if (FetchType.equalsIgnoreCase("file")) {
            String fileName = Environment.UC_ETC + "/" + ucConfig.getConfigurationFetchFileName();
            return new FileConfigurationFetcher(fileName);
        }
        throw new GuardUCException("Unknown ConfigurationFetcher, check UniversalConnector.properties, db.type");
    }
}
