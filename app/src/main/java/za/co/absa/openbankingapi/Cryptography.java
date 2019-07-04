package za.co.absa.openbankingapi;

import com.crashlytics.android.Crashlytics;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Cryptography {

    public static byte[] PasswordBasedKeyDerivationFunction2(String password, String salt, int numberOfRounds, int desiredKeySize) throws KeyGenerationFailureException, UnsupportedEncodingException {
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
        return PasswordBasedKeyDerivationFunction2(password, saltBytes, numberOfRounds, desiredKeySize);
    }

    public static byte[] PasswordBasedKeyDerivationFunction2(String password, byte[] saltBytes, int numberOfRounds, int desiredKeySize) throws KeyGenerationFailureException, UnsupportedEncodingException {
        char[] passwordBuffer = password.toCharArray();
        KeySpec keySpec = new PBEKeySpec(passwordBuffer, saltBytes, numberOfRounds, desiredKeySize);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return secretKeyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Crashlytics.logException(e);
            throw new KeyGenerationFailureException(e);
        }
    }
}