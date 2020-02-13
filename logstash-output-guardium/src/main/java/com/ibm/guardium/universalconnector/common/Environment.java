package com.ibm.guardium.universalconnector.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.util.Properties;


public class Environment {
    private static Log log = LogFactory.getLog(Environment.class);
    public static final String UDS_ETC = get("UDS_ETC", /*"/var/IBM/Guardium/collector/datastreams/etc"*/"C:\\Guard\\Git\\Guardium\\apps\\UniversalConnector\\logstash-output-java_output_example\\src\\resources\\");
    public static final Properties properties = loadProperties();
    public static final String MINI_SNIF_SSL_ENABLED = get("MINI_SNIF_SSL_ENABLED", "false");
    public static final String MINI_SNIF_PORT = get("MINI_SNIF_PORT", "16022");
    public static final String MINI_SNIF_HOSTNAME = get("MINI_SNIF_HOSTNAME", /*"9.70.156.216"*/"9.70.145.107"/*"9.32.128.106"*/);

    public static final String CONNECTOR_ID_PROP = "connector.id";
    public static final String CONNECTOR_IP_PROP = "connector.ip";

    public static final String CONNECTOR_ID = get(CONNECTOR_ID_PROP, "1");
    public static final String CONNECTOR_IP = get (CONNECTOR_IP_PROP, "127.0.0.1");

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
