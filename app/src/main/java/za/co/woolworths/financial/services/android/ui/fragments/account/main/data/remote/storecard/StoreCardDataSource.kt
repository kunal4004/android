package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.BaseDataSource
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