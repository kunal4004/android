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

import java.io.UnsupportedEncodingException;

public class AccountHolder {

    public PasscodeCredential generatePasscodeCredential(String passcode, String alias, String applicationIdentifier) throws AsymmetricCryptoHelper.AsymmetricEncryptionFailureException, AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException, KeyGenerationFailureException, UnsupportedEncodingException, DecryptionFailureException {

        SessionKey sessionKey = SessionKey.generate();

        byte[] encryptedAlias = SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), alias);

        byte[] derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(alias.concat(passcode), applicationIdentifier, 1000, 256);

        byte[] encryptedPasscode = SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), derivedKey);

        return new PasscodeCredential(encryptedPasscode, encryptedAlias, sessionKey);
    }

    public byte[] generateEncryptedPasscode(SessionKey sessionKey, String passcode, String alias, String applicationIdentifier) throws KeyGenerationFailureException, UnsupportedEncodingException, DecryptionFailureException {

        byte[] derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(alias.concat(passcode), applicationIdentifier, 1000, 256);

        return SymmetricCipher.Aes256Encrypt(sessionKey.getKey(), derivedKey);
    }
}