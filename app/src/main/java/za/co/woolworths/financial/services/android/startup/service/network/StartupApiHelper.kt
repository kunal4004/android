package za.co.woolworths.financial.services.android.startup.service.network

import android.content.Context
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartupApiHelper : RetrofitConfig() {
    suspend fun getConfig() = mApiInterface.getConfig(WoolworthsApplication.getApiId(),
            getSha1Password(),
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(),
            getOS(),
            getOsVersion(),
            getSessionToken(),
            getDeviceIdentityToken(),
            WoolworthsApplication.getAppVersionName())

    fun isConnectedToInternet(context: Context) = NetworkManager.getInstance().isConnectedToNetwork(context)
}