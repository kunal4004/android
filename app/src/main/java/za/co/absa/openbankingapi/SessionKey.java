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

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class SessionKey {
    private static final String OUTPUT_KEY_DERIVATION_ALGORITHM = "AES";
    public static final int OUTPUT_KEY_LENGTH = 256;
    public static final int OUTPUT_KEY_LENGTH_IV = 128;
    private byte[] key;
    private byte[] encryptedKey;
    private byte[] iv;

    private SessionKey(byte[] key, byte[] encryptedKey, byte[] iv) {
        this.key = key;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public byte[] getIV(){return iv;}

    public final String getEncryptedKeyBase64Encoded(){
        return Base64.encodeToString(encryptedKey, Base64.NO_WRAP);
    }

    public final String getEncryptedIVBase64Encoded(){
        return Base64.encodeToString(iv, Base64.NO_WRAP);
    }

    public static SessionKey generate() throws KeyGenerationFailureException,
            AsymmetricCryptoHelper.AsymmetricEncryptionFailureException,
            AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException {
        byte[] symmetricKey = generateKey(OUTPUT_KEY_LENGTH).getEncoded();
        byte[] encryptedSymmetricKeyBuffer = new AsymmetricCryptoHelper().encryptSymmetricKey(symmetricKey, WoolworthsApplication.getAbsaBankingOpenApiServices().getAppPublicKey());
        byte[] symmetricKeyIV = generateKey(OUTPUT_KEY_LENGTH_IV).getEncoded();
        return new SessionKey(symmetricKey, encryptedSymmetricKeyBuffer, symmetricKeyIV);
    }

    public static SecretKey generateKey(int keySize) throws KeyGenerationFailureException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(OUTPUT_KEY_DERIVATION_ALGORITHM);
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init(keySize, secureRandom);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            FirebaseManager.Companion.logException(e);
            throw new KeyGenerationFailureException(e);
        }
    }
}