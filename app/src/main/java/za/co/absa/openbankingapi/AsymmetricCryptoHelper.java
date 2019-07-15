/*
 *  Copyright (c) 2018 Absa Bank Limited, All Rights Reserved.
 *
 *  This code is confidential to Absa Bank Limited and shall not be disclosed
 *  outside the Bank without the prior written permission of the Absa Legal
 *
 *  In the event that such disclosure is permitted the code shall not be copied
 *  or distributed other than on a need-to-know basis and any recipients may be
 *  required to sign a confidentiality undertaking in favor of Absa Bank
 *  Limited
 *
 */
package za.co.absa.openbankingapi;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AsymmetricCryptoHelper {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";


    public final byte[] encryptSymmetricKey(byte[] symmetricKey, String pubKey) throws AsymmetricKeyGenerationFailureException, AsymmetricEncryptionFailureException {
        PublicKey publicKey = loadPublicKey(pubKey);
        return encrypt(publicKey, symmetricKey);
    }

    private PublicKey loadPublicKey(String pubKey) throws AsymmetricKeyGenerationFailureException {
        try {

            String key = pubKey.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");

            byte[] keyBytes = Base64.decode(key, Base64.DEFAULT);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e) {
            Crashlytics.logException(e);
            throw new AsymmetricKeyGenerationFailureException(e);
        }
    }

    private InputStream readPublicKeyFile(Context context, String keyFile) throws IOException {
        AssetManager assetManager = context.getAssets();
        return assetManager.open(keyFile);
    }

    private byte[] encrypt(PublicKey publicKey, byte[] plainData) throws AsymmetricEncryptionFailureException {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherData = cipher.doFinal(plainData);
            return cipherData;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            Crashlytics.logException(e);
            throw new AsymmetricEncryptionFailureException(e);
        }
    }

    public static final class AsymmetricEncryptionFailureException extends Exception {
        public AsymmetricEncryptionFailureException(Throwable e) {
            super(e);
        }
    }

    public static final class AsymmetricKeyGenerationFailureException extends Exception {
        public AsymmetricKeyGenerationFailureException(Throwable e) {
            super(e);
        }
    }
}