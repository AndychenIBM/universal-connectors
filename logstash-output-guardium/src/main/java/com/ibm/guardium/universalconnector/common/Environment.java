package com.ibm.guardium.universalconnector.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class Environment {
    private static Logger log = LogManager.getLogger(Environment.class);
    public static final String UC_ETC = "UC_ETC";
    public static final String UC_ETC_DEFAULT_PATH="C:\\Guard\\Git\\universal-connector\\logstash-output-guardium\\src\\resources\\";//"/tmp/logstash_plugin/";

    public static final String LOG42_CONF="log4j2uc.properties";

    public static final String UC_EXTERNAL_CONFIG = "UC_EXTERNAL_CONFIG";
    public static final String PERSISTENT_CONFIGURATION_FILE_NAME = "PersistentConfig.json";

    public static String getUcEtc() {
        String gEnv = System.getenv(UC_ETC);

        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(UC_ETC);
            System.out.println("Using UC_ETC System.getProperty");
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = UC_ETC_DEFAULT_PATH;
            System.out.println("Using UC_ETC_DEFAULT_PATH");
        }
        return gEnv;
    }

    public static String getUcExternalConfig() {
        String gEnv = System.getenv(UC_EXTERNAL_CONFIG);

        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(UC_EXTERNAL_CONFIG);
            System.out.println("Using UC_EXTERNAL_CONFIG System.getProperty");
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = getUcEtc();
            System.out.println("Using getUcEtc");
        }

        return gEnv;
    }

    public static String getPersistentConfigurationPath(){
        return getUcExternalConfig()+File.separator+PERSISTENT_CONFIGURATION_FILE_NAME;
    }

    public static String getLog42Conf() {
        String gEnv = getUcEtc();
        return gEnv+ File.separator+LOG42_CONF;
    }

}
