package za.co.woolworths.financial.services.android.ui.fragments.integration.service.validate_card_and_pin

import za.co.absa.openbankingapi.SessionKey
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.NetworkState

interface IValidateCardAndPin {
    fun createCardAndPinRequestProperty(
        cardPin: String?,
        cardToken: String?
    ): ValidateCardAndPinRequestProperty

    fun encryptCardPin(cardPin: String?, sessionKey: SessionKey?): String?
    suspend fun fetchValidateCardAndPin(
        cardPin: String?,
        cardToken: String?
    ): NetworkState<AbsaProxyResponseProperty>
}