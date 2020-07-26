package com.ibm.guardium.universalconnector.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class Environment {
    private static Logger log = LogManager.getLogger(Environment.class);
    public static final String UC_ETC = "UC_ETC";
    public static final String UC_ETC_DEFAULT_PATH="/tmp/logstash_plugin/";

    public static final String LOG42_CONF="log4j2uc.properties";
    public static final String DEFAULT_UC_UTC="/tmp/logstash_plugin/";

    public static String getUcEtc() {
        String gEnv = System.getenv(UC_ETC);
        System.out.println("System.getenv "+gEnv);

        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(UC_ETC);
            System.out.println("System.getProperty "+gEnv);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = UC_ETC_DEFAULT_PATH;
            System.out.println("failed to find UC_ETC, using default "+gEnv);
        }
        return gEnv;
    }

    public static String getLog42Conf() {
        String gEnv = getUcEtc();
        return gEnv+ File.separator+LOG42_CONF;
    }

}
