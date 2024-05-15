package com.ibm.guardium.universalconnector.common;


import com.ibm.guardium.universalconnector.services.CipherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;


public class Environment {
    private static Logger log = LogManager.getLogger(Environment.class);
    public static final String UC_ETC = "UC_ETC";
    public static final String UC_ETC_DEFAULT_PATH=System.getProperty("user.dir")+File.separator+"src"+File.separator+"resources"+File.separator;//"/tmp/logstash_plugin/";

    public static final String LOG42_CONF="log4j2uc.properties";

    public static final String UC_EXTERNAL_CONFIG = "UC_EXTERNAL_CONFIG";
    public static final String PERSISTENT_CONFIGURATION_FILE_NAME = "PersistentConfig.json";

    public static final String INSIGHT_KEYSTORE_PATH = getEnvOrDefault("INSIGHT_KEYSTORE_PATH",
            "/service/certs/universalconnector/insights.jks");

    public static final String UC_TLS_VERSION = getEnvOrDefault("UC_TLS_VERSION", "TLSv1.2");

    public static final String UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD = getEnvOrDefault("UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD", "");

    //cipher
    public static final String ENCRYPTION_ALG = getEnvOrDefault("ENCRYPTION_ALG", "");
    public static final String MASTER_KEY = getEnvOrDefault("MASTER_KEY", "");
    public static final String MASTER_AAD = getEnvOrDefault("MASTER_AAD", "");
    public static final String ENCRYPTION_PASSWORD = getEnvOrDefault("ENCRYPTION_PASSWORD", "");
    public static final String GCM_AAD = getEnvOrDefault("GCM_AAD", "");

    private static String getEnvOrDefault(String envName, String defaultVal) {
        String value = System.getenv(envName);
        if ((value == null) || (value.isEmpty())) {
            return defaultVal;
        }
        return value;
    }

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
