package za.co.woolworths.financial.services.android.models.network

import okhttp3.Interceptor
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.API_ID
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.APP_VERSION
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.DEVICE_MODEL
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.DEVICE_VERSION
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.NETWORK
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.OS
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils.Companion.SHA1_PASSWORD

class CommonHeaderInterceptor : NetworkConfig() , Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
                .newBuilder()
                .addHeader(API_ID, getApiId())
                .addHeader(SHA1_PASSWORD, getSha1Password())
                .addHeader(DEVICE_VERSION, getDeviceManufacturer())
                .addHeader(DEVICE_MODEL, getDeviceModel())
                .addHeader(NETWORK, getNetworkCarrier())
                .addHeader(OS, getOS())
                .addHeader(CommonHeaderUtils.OS_VERSION, getOsVersion())
                .addHeader(APP_VERSION,getAppVersion())
        return chain.proceed(request.build())
    }
}
