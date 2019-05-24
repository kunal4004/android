package za.co.woolworths.financial.services.android.models.network

import com.awfs.coordination.BuildConfig
import retrofit2.Call
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse

object OneAppService : RetrofitService() {
    fun getConfig(): Call<ConfigResponse> = mApiInterface.getConfig(
            WoolworthsApplication.getApiId(),
            BuildConfig.SHA1,
            getDeviceManufacturer(),
            getDeviceModel(),
            getNetworkCarrier(appContext()),
            getOS(),
            getOsVersion(),
            getSessionToken(),
            WoolworthsApplication.getAppVersionName())
}