package com.ibm.guardium.universalconnector.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;


public class Environment {
    private static Logger log = LogManager.getLogger(Environment.class);
    public static final String UC_ETC = "UC_ETC";
    public static final String UDS_ETC = get("UDS_ETC", "C:\\Guard\\Git\\universal-connector\\logstash-output-guardium\\src\\resources");
    public static final Properties properties = loadProperties();
    public static final String MINI_SNIF_SSL_ENABLED = get("MINI_SNIF_SSL_ENABLED", "false");
    public static final String MINI_SNIF_PORT = get("MINI_SNIF_PORT", "16022");
    public static final String MINI_SNIF_HOSTNAME = get("MINI_SNIF_HOSTNAME", "9.70.157.70"/*"9.70.156.216"*//*"9.70.145.107"*//*"9.32.128.106"*/);

    public static final String CONNECTOR_ID_PROP = "connector.id";
    public static final String CONNECTOR_IP_PROP = "connector.ip";

    public static final String CONNECTOR_ID = get(CONNECTOR_ID_PROP, "1");
    public static final String CONNECTOR_IP = get (CONNECTOR_IP_PROP, "127.0.0.1");

    public static final String LOG42_CONF="log4j2uc.properties";

    public static String getUcEtc() {
        String gEnv = System.getenv(UC_ETC);
        System.out.println("System.getenv "+gEnv);

        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(UC_ETC);
            System.out.println("System.getProperty "+gEnv);
        }
        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = "/tmp/logstash_plugin/";
            System.out.println("failed to find UC_ETC, using default "+gEnv);
        }
        return gEnv;
    }

    public static String getLog42Conf() {
        String gEnv = System.getenv(UC_ETC);
        System.out.println("System.getenv "+gEnv);

        if (null == gEnv || gEnv.isEmpty()) {
            gEnv = System.getProperty(UC_ETC);
            System.out.println("System.getProperty "+gEnv);
        }
        return gEnv+LOG42_CONF;
    }

    private static String get(String envName, String defaultVal) {
        String gEnv = System.getenv(envName);
        if (null == gEnv || gEnv.isEmpty()) {
            if (properties != null) {
                defaultVal = properties.getProperty(envName, defaultVal);
            }
            gEnv = System.getProperty(envName, null != defaultVal ? defaultVal : "");
        }
        return gEnv;
    }

    private static Properties loadProperties(){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(UDS_ETC + "/UniversalConnector.properties"));
        } catch (Exception e){
            log.error("failed to load properties file at:" + UDS_ETC + "/UniversalConnector.properties");
        }
        return properties;
    }
}
