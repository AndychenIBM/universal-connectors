package com.ibm.guardium.universalconnector.services;

import com.ibm.guardium.universalconnector.common.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class CipherService {

    private static final Logger log = LogManager.getLogger(CipherService.class);

    private static CipherService cipherService;

    private CipherService() {}

    public static synchronized CipherService getInstance ()
    {
        if(cipherService == null) {
            cipherService = new CipherService();
        }
        return cipherService;
    }

    public String decryptUCKeyStorePass() throws Exception{
        return decryptWithMaster(Environment.UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD);
    }

    private String decryptWithMaster(String passphrase) throws Exception {
            try {
                if (passphrase.equals("")) {
                    return "";
                }
                log.debug("Decrypting secret using GCM");

                int gcmIvLength = 12;
                int gcmTagLength = 16;
                int gcmSaltLength = 64;

                final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                byte[] cipherMessage = Base64.getDecoder().decode(passphrase);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

                byte[] salt = Arrays.copyOfRange(cipherMessage, 0, gcmSaltLength);

                String masterKey = Environment.MASTER_KEY;
                KeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt, 65536, 32 * 8);
                SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

                GCMParameterSpec parameterSpec = new GCMParameterSpec(gcmTagLength * 8, cipherMessage,
                        gcmSaltLength, gcmIvLength);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
                cipher.updateAAD(Environment.MASTER_AAD.getBytes());

                byte[] plainText = cipher.doFinal(cipherMessage, gcmIvLength + gcmSaltLength,
                        cipherMessage.length - gcmSaltLength - gcmIvLength);

                return new String(plainText).trim();
            } catch (Exception e) {
                log.error("could not decrypt keystore password, ", e);
                throw e;
            }
        }

}
