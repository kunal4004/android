package za.co.woolworths.financial.services.android.util.encryption;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Cryptography {

    public static byte[] PasswordBasedKeyDerivationFunction2(String password, String salt, int numberOfRounds, int desiredKeySize) throws KeyGenerationFailureException, UnsupportedEncodingException {
        char[] passwordBuffer = password.toCharArray();
        byte[] saltBytes = salt.getBytes("UTF-8");
        KeySpec keySpec = new PBEKeySpec(passwordBuffer, saltBytes, numberOfRounds, desiredKeySize);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new KeyGenerationFailureException(e);
        }
    }
}