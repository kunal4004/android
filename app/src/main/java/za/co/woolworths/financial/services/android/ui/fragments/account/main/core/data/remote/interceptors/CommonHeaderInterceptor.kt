package za.co.woolworths.financial.services.android.ui.fragments.account.main.core.data.remote.interceptors

import com.amazonaws.util.VersionInfoUtils.getUserAgent
import okhttp3.Interceptor
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils
import za.co.woolworths.financial.services.android.models.network.NetworkConfig

class CommonHeaderInterceptor : NetworkConfig(), Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
            .newBuilder()
            .addHeader(CommonHeaderUtils.API_ID, getApiId())
            .addHeader(CommonHeaderUtils.SHA1_PASSWORD, getSha1Password())
            .addHeader(CommonHeaderUtils.DEVICE_VERSION, getDeviceManufacturer())
            .addHeader(CommonHeaderUtils.DEVICE_MODEL, getDeviceModel())
            .addHeader(CommonHeaderUtils.NETWORK, getNetworkCarrier())
            .addHeader(CommonHeaderUtils.OS, getOS())
            .addHeader(CommonHeaderUtils.USER_AGENT, "")
            .addHeader(CommonHeaderUtils.USER_AGENT_VERSION, "")
            .addHeader(CommonHeaderUtils.OS_VERSION, getOsVersion())
            .addHeader(CommonHeaderUtils.SESSION_TOKEN, getSessionToken())
        return chain.proceed(request.build())
    }
}