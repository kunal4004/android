package za.co.woolworths.financial.services.android.ui.fragments.integration.service.register_credential

import android.util.Base64
import za.co.absa.openbankingapi.Cryptography
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.KeyGenerationFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.extension.json
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.SessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils
import java.io.UnsupportedEncodingException

class AbsaRegisterCredentialsImpl(private val sessionKeyGenerator: SessionKeyGenerator) :
    IAbsaRegisterCredentials {

    override val mobileApp5DigitPin: String
        get() = "MOBILEAPP_5DIGIT_PIN"

    override fun getAbsaUniqueDeviceId(): String? = Utils.getAbsaUniqueDeviceID()

    override fun getCredentialsVOs(encryptedAlias: String?, base64EncodedEncryptedDerivedKey: String): Array<CredentialVO?> {
        val credentialVOs = arrayOfNulls<CredentialVO?>(1)

        credentialVOs[0] =  CredentialVO(encryptedAlias, mobileApp5DigitPin, base64EncodedEncryptedDerivedKey)
        return credentialVOs
    }

    override fun createRegisterCredentialsRequestBody(aliasId: String?, passcode: String?): AbsaRegisterCredentialRequestProperty? {
        try {
            val deviceId = getAbsaUniqueDeviceId()
            val sessionKey = sessionKeyGenerator.generateSessionKey()
            val symmetricKey = sessionKeyGenerator.getSymmetricKey(sessionKey)
            val symmetricKeyIV = sessionKeyGenerator.getIV(sessionKey)
            val encryptedAlias = sessionKeyGenerator.convertStringToSymmetricCipherAes256EncryptAndBase64Encode(aliasId, sessionKey)

            val derivedKey: ByteArray = Cryptography.PasswordBasedKeyDerivationFunction2(aliasId + passcode, deviceId, 1000, 256)

            val encryptedDerivedKey = SymmetricCipher.Aes256Encrypt(symmetricKey, derivedKey, symmetricKeyIV)

            val base64EncodedEncryptedDerivedKey: String = Base64.encodeToString(encryptedDerivedKey, Base64.NO_WRAP)

            val credentialVOs = getCredentialsVOs(encryptedAlias, base64EncodedEncryptedDerivedKey)

            return AbsaRegisterCredentialRequestProperty(Header(), encryptedAlias, deviceId, credentialVOs, sessionKeyGenerator.getGatewaySymmetricKey(sessionKey), sessionKeyGenerator.getEncryptedIVBase64Encoded(sessionKey))

        } catch (e: DecryptionFailureException) {
            FirebaseManager.logException(e)
        } catch (e: UnsupportedEncodingException) {
            FirebaseManager.logException(e)
        } catch (e: KeyGenerationFailureException) {
            FirebaseManager.logException(e)
        }

        return null
    }

    override suspend fun fetchAbsaRegisterCredentials(aliasId: String?, passcode: String?): NetworkState<AbsaProxyResponseProperty> {
        val registerCredentialsRequest = createRegisterCredentialsRequestBody(aliasId, passcode)?.json()
        registerCredentialsRequest?.contentLength()
        val withEncryptedBody = registerCredentialsRequest?.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceRegisterCredentials( AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }
}