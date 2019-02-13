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
import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SessionKey {
    private static final String OUTPUT_KEY_DERIVATION_ALGORITHM = "AES";
    private static final int OUTPUT_KEY_LENGTH = 256;
    private byte[] key;
    private byte[] encryptedKey;

    private SessionKey(byte[] key, byte[] encryptedKey) {
        this.key = key;
        this.encryptedKey = encryptedKey;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public final String getEncryptedKeyBase64Encoded(){
        return Base64.encodeToString(encryptedKey, Base64.NO_WRAP);
    }

    public static SessionKey generate(Context context) throws KeyGenerationFailureException,
            AsymmetricCryptoHelper.AsymmetricEncryptionFailureException,
            AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException {
        byte[] symmetricKey = generateKey().getEncoded();
        byte[] encryptedSymmetricKeyBuffer = new AsymmetricCryptoHelper().encryptSymmetricKey(context, symmetricKey);
        return new SessionKey(symmetricKey, encryptedSymmetricKeyBuffer);
    }

    private static SecretKey generateKey() throws KeyGenerationFailureException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(OUTPUT_KEY_DERIVATION_ALGORITHM);
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(OUTPUT_KEY_LENGTH, secureRandom);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new KeyGenerationFailureException(e);
        }
    }
}