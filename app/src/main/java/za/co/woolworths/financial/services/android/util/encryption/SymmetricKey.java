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

public class SymmetricKey {
    private String safeUrlEncodedEncryptedSymmetricKey;

    public SymmetricKey(byte[] encryptedSymmetricKey) {
        safeUrlEncodedEncryptedSymmetricKey = Base64.encodeBytes(encryptedSymmetricKey);
    }

    public String getSafeUrlEncodedEncryptedSymmetricKey() {
        return safeUrlEncodedEncryptedSymmetricKey;
    }
}

