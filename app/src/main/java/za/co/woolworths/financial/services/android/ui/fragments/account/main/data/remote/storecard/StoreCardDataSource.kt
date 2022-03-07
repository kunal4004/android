package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.BaseDataSource
import javax.inject.Inject

class StoreCardDataSource @Inject constructor(
    private val storeCardService: StoreCardService) : BaseDataSource() {
    suspend fun getCreditCardToken() = getResult {
        storeCardService.getCreditCardToken(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }
    suspend fun getPaymentPAYUMethod() = getResult {
        storeCardService.getPaymentPAYUMethod(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }
}