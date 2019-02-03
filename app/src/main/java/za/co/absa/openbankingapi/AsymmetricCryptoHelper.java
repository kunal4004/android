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
    private static final String KEY_FILE = "mobileapp.pem.cer";

    public final byte[] encryptSymmetricKey(Context context, byte[] symmetricKey) throws AsymmetricKeyGenerationFailureException, AsymmetricEncryptionFailureException {
        PublicKey publicKey = loadPublicKey(context);
        return encrypt(publicKey, symmetricKey);
    }

    private PublicKey loadPublicKey(Context context) throws AsymmetricKeyGenerationFailureException {
        try {
            InputStream inputStream = readPublicKeyFile(context);

            byte[] keyBytes = new byte[inputStream.available()];
            inputStream.read(keyBytes);
            inputStream.close();

            String pubKey = new String(keyBytes, "UTF-8");
            pubKey = pubKey.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");

            keyBytes = Base64.decode(pubKey);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NullPointerException e) {
            throw new AsymmetricKeyGenerationFailureException(e);
        }
    }

    private InputStream readPublicKeyFile(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        return assetManager.open(KEY_FILE);
    }

    private byte[] encrypt(PublicKey publicKey, byte[] plainData) throws AsymmetricEncryptionFailureException {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] cipherData = cipher.doFinal(plainData);
            return cipherData;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
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