// **************************************************************
//
// IBM Confidential
//
// OCO Source Materials
//
// 5737-L66
//
// (C) Copyright IBM Corp. 2019, 2024
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// **************************************************************

import java.util.Base64;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.TrustManagerFactory;
import java.security.spec.KeySpec;

public class GCMDecrypt {
    public static void main(String[] args) {
        String secret = args[0];

        int GCM_IV_LENGTH = 12;
        int GCM_TAG_LENGTH = 16;
        int GCM_SALT_LENGTH = 64;

        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] cipherMessage = Base64.getDecoder().decode(secret);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] salt = Arrays.copyOfRange(cipherMessage, 0, GCM_SALT_LENGTH);

            String masterKey = System.getenv("MASTER_KEY");
            KeySpec spec = new PBEKeySpec(masterKey.toCharArray(), salt, 65536, 32 * 8);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH*8, cipherMessage, GCM_SALT_LENGTH, GCM_IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            cipher.updateAAD(System.getenv("MASTER_AAD").getBytes());
            
            byte[] plainText = cipher.doFinal(cipherMessage, GCM_IV_LENGTH + GCM_SALT_LENGTH, cipherMessage.length - GCM_SALT_LENGTH - GCM_IV_LENGTH);
            System.out.print(new String(plainText).trim());
        } catch (Exception e) {
            System.err.println("Failed Decrypt");
            e.printStackTrace();
        }
    }
}
