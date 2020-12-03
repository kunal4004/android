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

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import za.co.woolworths.financial.services.android.util.FirebaseManager;

public class SymmetricCipher {

    public static final byte[] Aes256Encrypt(byte[] keyBytes, String data) throws DecryptionFailureException {
        return Aes256Encrypt(keyBytes, data.getBytes(StandardCharsets.UTF_8));
    }

    public static final String Aes256EncryptAndBase64Encode(String string, byte[] keyBytes, byte[] iv) throws DecryptionFailureException {
        byte[] encryptedData = Aes256Encrypt(keyBytes, string.getBytes(StandardCharsets.UTF_8), iv);
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }

    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     *
     * This method is responsible for executing
     * Aes256Encrypt with a default zero based IV.
     */
    public static final byte[] Aes256Encrypt(byte[] keyBytes, byte[] data) throws DecryptionFailureException {
        return Aes256Encrypt(keyBytes, data, generateIV());
    }

    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     * @param {byte[]} iv
     *
     * This method is responsible for executing
     * Aes256Encrypt casting the IV bytes into IvParameterSpec.
     */
    public static final byte[] Aes256Encrypt(byte[] keyBytes, byte[] data, byte[] iv) throws DecryptionFailureException {
        AlgorithmParameterSpec algorithmParameterSpec = new IvParameterSpec(iv);
        return Aes256Encrypt(keyBytes, data, algorithmParameterSpec);
    }

    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     * @param {AlgorithmParameterSpec} algorithmParameterSpec
     *
     * This is the main method used for AES Encryption.
     */
    public static final byte[] Aes256Encrypt(byte[] keyBytes, byte[] data, AlgorithmParameterSpec algorithmParameterSpec) throws DecryptionFailureException {
        try {
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, algorithmParameterSpec);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalArgumentException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            FirebaseManager.Companion.logException(e);
            throw new DecryptionFailureException(e);
        }
    }





    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     *
     * This method is responsible for executing
     * Aes256Decrypt with a default zero based IV.
     */
    public static final byte[] Aes256Decrypt(byte[] keyBytes, byte[] data) throws DecryptionFailureException {
        return Aes256Decrypt(keyBytes, data, generateIV());
    }

    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     * @param {byte[]} iv
     *
     * This method is responsible for executing
     * Aes256Encrypt casting the IV bytes into IvParameterSpec.
     */
    public static final byte[] Aes256Decrypt(byte[] keyBytes, byte[] data, byte[] iv) throws DecryptionFailureException {
        AlgorithmParameterSpec algorithmParameterSpec = new IvParameterSpec(iv);
        return Aes256Decrypt(keyBytes, data, algorithmParameterSpec);
    }

    /**
     *
     * @param {byte[]} keyBytes
     * @param {byte[]} data
     * @param {AlgorithmParameterSpec} algorithmParameterSpec
     *
     * This is the main method used for AES decryption.
     */
    public static final byte[] Aes256Decrypt(byte[] keyBytes, byte[] data, AlgorithmParameterSpec algorithmParameterSpec) throws DecryptionFailureException {
        try {
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, algorithmParameterSpec);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalArgumentException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            FirebaseManager.Companion.logException(e);
            throw new DecryptionFailureException(e);
        }
    }

    private static AlgorithmParameterSpec generateIV() {
        final int ivLength = 16;
        byte[] ivBuffer = new byte[ivLength];
        Arrays.fill(ivBuffer, (byte) 0x00);
        return new IvParameterSpec(ivBuffer);
    }
}