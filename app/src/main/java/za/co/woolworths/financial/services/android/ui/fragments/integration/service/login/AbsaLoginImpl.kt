package za.co.woolworths.financial.services.android.ui.fragments.integration.service.login

import android.util.Base64
import za.co.absa.openbankingapi.Cryptography
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.KeyGenerationFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.absa.openbankingapi.woolworths.integration.AbsaSecureCredentials
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.SessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.contentLength
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.resultOf
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toAes256Encrypt
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.Utils
import java.io.UnsupportedEncodingException

class AbsaLoginImpl(private val sessionKeyGenerator: SessionKeyGenerator) : IAbsaLogin {

    override fun getAbsaUniqueDeviceId(): String?  = Utils.getAbsaUniqueDeviceID()

    override fun requestBody(passcode: String): String? {

        val aliasId = AbsaSecureCredentials().aliasId
        val deviceId = getAbsaUniqueDeviceId() ?: ""
        val sessionKey = sessionKeyGenerator.generateSessionKey()
        val gatewaySymmetricKey: String = sessionKeyGenerator.getGatewaySymmetricKey(sessionKey) ?: ""

        val base64EncodedEncryptedDerivedKey: String?
        val encryptedAlias: String?

       return try {
            encryptedAlias = Base64.encodeToString(SymmetricCipher.Aes256Encrypt(sessionKey?.key, aliasId), Base64.NO_WRAP)
            val derivedKey = Cryptography.PasswordBasedKeyDerivationFunction2(aliasId + passcode, deviceId, 1000, 256)
            val encryptedDerivedKey = SymmetricCipher.Aes256Encrypt(sessionKey?.key, derivedKey)
            base64EncodedEncryptedDerivedKey = Base64.encodeToString(encryptedDerivedKey, Base64.NO_WRAP)
            LoginRequestProperty(encryptedAlias, deviceId, base64EncodedEncryptedDerivedKey, gatewaySymmetricKey, sessionKey?.encryptedIVBase64Encoded ?: "").urlEncodedFormData
        } catch (e: UnsupportedEncodingException) {
            logException(e)
           null
        } catch (e: DecryptionFailureException) {
            logException(e)
            null

        } catch (e: UnsupportedEncodingException) {
            logException(e)
            null
        } catch (e: KeyGenerationFailureException) {
            logException(e)
            null
        }
    }

    override suspend fun fetchAbsaLogin(passcode: String): NetworkState<AbsaProxyResponseProperty> {
        val loginRequestBody = requestBody(passcode)
        loginRequestBody?.contentLength()
        val withEncryptedBody = loginRequestBody?.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceLogin(AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }

}