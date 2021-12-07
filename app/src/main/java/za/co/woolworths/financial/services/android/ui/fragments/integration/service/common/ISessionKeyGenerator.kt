package za.co.woolworths.financial.services.android.ui.fragments.integration.service.common

import za.co.absa.openbankingapi.SessionKey

interface ISessionKeyGenerator {
    fun generateSessionKey(): SessionKey?
    fun getGatewaySymmetricKey(sessionKey: SessionKey?): String?
    fun getEncryptedIVBase64Encoded(sessionKey: SessionKey?): String?
    fun getSymmetricKey(sessionKey: SessionKey?):ByteArray?
    fun getIV(sessionKey: SessionKey?):ByteArray?
    fun convertStringToSymmetricCipherAes256EncryptAndBase64Encode(text: String?, sessionKey: SessionKey?): String?
}