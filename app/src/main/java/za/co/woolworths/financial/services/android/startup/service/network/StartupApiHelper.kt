package za.co.woolworths.financial.services.android.startup.service.network

import android.content.Context
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartupApiHelper : RetrofitConfig(AppContextProviderImpl(), RetrofitApiProviderImpl()) {
    suspend fun getConfig() = mApiInterface.getConfig(
            getSessionToken(),
            getDeviceIdentityToken(),
            WoolworthsApplication.getAppVersionName())

    suspend fun getCartSummary() = mApiInterface.getCartsSummary(
            getSessionToken(),
            getDeviceIdentityToken())

    fun isConnectedToInternet(context: Context) = NetworkManager.getInstance().isConnectedToNetwork(context)
}