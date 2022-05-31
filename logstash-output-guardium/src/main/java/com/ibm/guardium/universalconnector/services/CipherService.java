package com.ibm.guardium.universalconnector.services;

import com.ibm.guardium.universalconnector.common.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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

    public String decryptUCKeyStorePass() {
        return decryptWithMaster(Environment.UNIVERSAL_CONNECTOR_KEYSTORE_PASSWORD);
    }

    private String decryptWithMaster(String passphrase) {
            try {
                if (passphrase.equals("")) {
                    return "";
                }
                if (Environment.ENCRYPTION_ALG.equals("GCM")) {
                    log.info("Decrypting secret using GCM");

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
                } else {
                    log.info("Decrypting secret using CBC");
                    System.out.println("Decrypting secret using CBC");

                    byte[] cypher = Base64.getDecoder().decode(passphrase);
                    byte[] contents = Arrays.copyOfRange(cypher, 16, cypher.length);
                    byte[] salt = Arrays.copyOfRange(cypher, 8, 16);
                    String secret = Environment.MASTER_KEY;
                    PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 10000, 48 * 8);
                    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    byte[] keyIv = skf.generateSecret(spec).getEncoded();
                    byte[] key = Arrays.copyOfRange(keyIv, 0, 32);
                    byte[] iv = Arrays.copyOfRange(keyIv, 32, 48);

                    SecretKey aesKey = new SecretKeySpec(key, "AES");
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));

                    byte[] result = cipher.doFinal(contents);
                    return (new String(result).trim());
                }
            } catch (Exception e) {
                log.error("could not decrypt keystore password, ", e);
                return "";
            }
        }

}
