package za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton

class CommonAbsaHeadersInterceptor : Interceptor, RetrofitConfig() {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        val requestBuilder = request().newBuilder()

        with(requestBuilder) {
            header("sessionToken",getSessionToken())
            val  keyId = AbsaTemporaryDataSourceSingleton.keyId
            AbsaTemporaryDataSourceSingleton.jsessionId?.let { addHeader("JSESSIONID", it) }

            val contentLength = AbsaTemporaryDataSourceSingleton.contentLength
            if (contentLength != null && contentLength > 0 && keyId?.isNotEmpty() == true) {
                addHeader("x-encrypted", "$contentLength|$keyId")
            }
        }
        proceed(requestBuilder.build())
    }

}