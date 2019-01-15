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
package za.co.woolworths.financial.services.android.util.encryption;

public abstract class BaseCredential {

    private byte[] encryptedPasscode;
    private byte[] encryptedAliasId;
    private SessionKey sessionKey;

    public BaseCredential(byte[] encryptedPasscode, byte[] encryptedAliasId, SessionKey sessionKey) {
        this.encryptedPasscode = encryptedPasscode;
        this.encryptedAliasId = encryptedAliasId;
        this.sessionKey = sessionKey;
    }

    public byte[] getEncryptedAliasId() {
        return encryptedAliasId;
    }

    public byte[] getEncryptedPasscode() {
        return encryptedPasscode;
    }

    public SessionKey getSessionKey() {
        return sessionKey;
    }
}
