package com.ibm.guardium.universalconnector.common;

// **************************************************************
//
// IBM Confidential
//
// OCO Source Materials
//
// 5737-L66
//
// (C) Copyright IBM Corp. 2019, 2023
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// **************************************************************

import com.ibm.guardium.universalconnector.services.CipherService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class InsightsTrustManager {

    private static final Logger log = LogManager.getLogger(InsightsTrustManager.class);

    private static TrustManagerFactory tmf = null;

    private InsightsTrustManager() {}

    public static synchronized TrustManagerFactory getInstance() throws Exception {
        // return cached TrustManagerFactory
        if (tmf != null) {
            return tmf;
        }
        try (FileInputStream fis = new FileInputStream(Environment.INSIGHT_KEYSTORE_PATH)) {
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            String keystorePassword = CipherService.getInstance().decryptUCKeyStorePass();
            ks.load(fis, keystorePassword.toCharArray());
            // Convert keystore to trust manager factory
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            log.info("using trust manager, keystore path:" + Environment.INSIGHT_KEYSTORE_PATH);
            return tmf;
        }
    }
}
