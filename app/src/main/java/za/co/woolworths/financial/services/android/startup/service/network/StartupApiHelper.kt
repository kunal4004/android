package za.co.woolworths.financial.services.android.startup.service.network

import android.content.Context
import android.util.Log
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitApiProviderImpl
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartupApiHelper : RetrofitConfig(AppContextProviderImpl(), RetrofitApiProviderImpl()) {
    suspend fun getConfig(): ConfigResponse {
        Log.d("TAG_DATA_APP", "ConfigResponse_ConfigResponse_ConfigResponse_ConfigResponse_ConfigResponse_ConfigResponse")
        return mApiInterface.getConfig(
            getSessionToken(),
            getDeviceIdentityToken(),
            WoolworthsApplication.getAppVersionName())
    }

    fun isConnectedToInternet(context: Context) = NetworkManager.getInstance().isConnectedToNetwork(context)
}