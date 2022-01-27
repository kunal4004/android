package za.co.woolworths.financial.services.android.chanel.services.network

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

class ChanelApiHelper : RetrofitConfig() {

    suspend fun getBanners() = mApiInterface.getConfig(
        getSessionToken(),
        getDeviceIdentityToken(),
        WoolworthsApplication.getAppVersionName())
}