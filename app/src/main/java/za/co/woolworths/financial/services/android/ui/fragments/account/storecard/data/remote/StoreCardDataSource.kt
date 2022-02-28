package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote

import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import javax.inject.Inject

class StoreCardDataSource @Inject constructor(
    private val storeCardService: StoreCardService,
    private val networkConfig: NetworkConfig
) : BaseDataSource() {
    suspend fun getCreditCardToken() = getResult {
        storeCardService.getCreditCardToken(
            "", "",
            networkConfig.getSessionToken(),
            networkConfig.getDeviceIdentityToken()
        )
    }

}