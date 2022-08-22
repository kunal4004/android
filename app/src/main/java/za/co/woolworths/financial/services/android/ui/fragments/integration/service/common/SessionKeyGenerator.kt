package za.co.woolworths.financial.services.android.ui.fragments.integration.service.common

import za.co.absa.openbankingapi.AsymmetricCryptoHelper
import za.co.absa.openbankingapi.KeyGenerationFailureException
import za.co.absa.openbankingapi.SessionKey
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager

class SessionKeyGenerator : ISessionKeyGenerator {

    override fun generateSessionKey(): SessionKey? {
        return try {
            SessionKey.generate()
        } catch (e: KeyGenerationFailureException) {
            FirebaseManager.logException(e)
            null
        } catch (e: AsymmetricCryptoHelper.AsymmetricEncryptionFailureException) {
            FirebaseManager.logException(e)
            null
        } catch (e: AsymmetricCryptoHelper.AsymmetricKeyGenerationFailureException) {
            FirebaseManager.logException(e)
            null
        }
    }

    override fun getGatewaySymmetricKey(sessionKey: SessionKey?): String? {
        return sessionKey?.encryptedKeyBase64Encoded
    }

    override fun getEncryptedIVBase64Encoded(sessionKey: SessionKey?): String? {
        return sessionKey?.encryptedIVBase64Encoded
    }

    override fun getSymmetricKey(sessionKey: SessionKey?): ByteArray? {
        return sessionKey?.key
    }

    override fun getIV(sessionKey: SessionKey?): ByteArray? {
        return sessionKey?.iv
    }

    override fun convertStringToSymmetricCipherAes256EncryptAndBase64Encode(text: String?, sessionKey: SessionKey?): String? {
        return SymmetricCipher.Aes256EncryptAndBase64Encode(
            text,
            getSymmetricKey(sessionKey),
            getIV(sessionKey)
        )
    }
}