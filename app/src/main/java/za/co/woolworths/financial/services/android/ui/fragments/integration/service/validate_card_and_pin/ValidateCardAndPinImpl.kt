package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin

import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.SessionKey
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.absa.openbankingapi.woolworths.integration.dto.Header
import za.co.woolworths.financial.services.android.ui.extension.json
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.AbsaRemoteApi
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.common.ISessionKeyGenerator
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException

class ValidateCardAndPinImpl(private val sessionKeyGenerator: ISessionKeyGenerator) : IValidateCardAndPin {

    override fun createCardAndPinRequestProperty(
        cardPin: String?,
        cardToken: String?
    ): ValidateCardAndPinRequestProperty {
        val sessionKey = sessionKeyGenerator.generateSessionKey()
        val gatewaySymmetricKey = sessionKeyGenerator.getGatewaySymmetricKey(sessionKey)
        val encryptedIVBase64Encoded = sessionKeyGenerator.getEncryptedIVBase64Encoded(sessionKey)
        val encryptedCardPin = encryptCardPin(cardPin, sessionKey)
        return ValidateCardAndPinRequestProperty(
                Header(),
                cardToken,
                encryptedCardPin,
                gatewaySymmetricKey,
                encryptedIVBase64Encoded
        )
    }

    override fun encryptCardPin(cardPin: String?, sessionKey: SessionKey?): String? {
        return try {
            SymmetricCipher.Aes256EncryptAndBase64Encode(cardPin, sessionKey?.key, sessionKey?.iv)
        } catch (e: DecryptionFailureException) {
            logException(e)
            null
        }
    }

    override suspend fun fetchValidateCardAndPin(
        cardPin: String?,
        cardToken: String?
    ): NetworkState<AbsaProxyResponseProperty> {
        val validateCardAndPinRequestProperty = createCardAndPinRequestProperty(cardPin, cardToken).json()
        validateCardAndPinRequestProperty.contentLength()
        val withEncryptedBody = validateCardAndPinRequestProperty.toAes256Encrypt()
        return resultOf(AbsaRemoteApi.service.queryAbsaServiceValidateCardAndPin(
            AbsaTemporaryDataSourceSingleton.cookie,withEncryptedBody))
    }
}