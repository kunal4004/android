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

import java.io.UnsupportedEncodingException;
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

public class SymmetricCipher {

    public static final byte[] Aes256Encrypt(byte[] keyBytes, String data) throws DecryptionFailureException, UnsupportedEncodingException {
        return Aes256Encrypt(keyBytes, data.getBytes("UTF-8"));
    }

    public static final String Aes256EncryptAndBase64Encode(String string, byte[] keyBytes) throws DecryptionFailureException, UnsupportedEncodingException {
        byte[] encryptedData = Aes256Encrypt(keyBytes, string.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
    }

    public static final byte[] Aes256Encrypt(byte[] keyBytes, byte[] data) throws DecryptionFailureException {
        try {
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, generateIV());
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalArgumentException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionFailureException(e);
        }
    }

    public static final byte[] Aes256Decrypt(byte[] keyBytes, byte[] data) throws DecryptionFailureException {
        try {
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, generateIV());
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalArgumentException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
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