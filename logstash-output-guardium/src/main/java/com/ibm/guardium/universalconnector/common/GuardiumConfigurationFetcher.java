package com.ibm.guardium.universalconnector.common;

import com.ibm.guardium.universalconnector.config.SnifferConfig;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Fetch from guardium configuration -
 * invoke grdapi to get configuration
 * change settings accordingly
 */
public class GuardiumConfigurationFetcher implements ConfigurationFetcher {
    @Override
    public List<SnifferConfig> fetch() throws FileNotFoundException {
        //---------------------------------------------------------------//
        // 1.Connect to guaridum via rest -
        //     1.1 Decrypt password for GUI user
        //     1.2 Decrypt secret for OAuth2 authentication
        //     1.4 Get token
        //     1.5 Invoke rest api
        //
        // 2.Process rest response and build configuration
        //
        // 3.Compare with existing configuration and replace if needed
        //
        //---------------------------------------------------------------//
        return null;
    }
}
