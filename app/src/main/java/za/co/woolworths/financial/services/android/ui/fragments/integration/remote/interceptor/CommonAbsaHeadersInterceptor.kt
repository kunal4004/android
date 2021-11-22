package za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.toEncryptedHex

class CommonAbsaHeadersInterceptor : Interceptor, RetrofitConfig() {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        val requestBuilder = request().newBuilder()

        with(requestBuilder) {
            header("sessionToken", getSessionToken())

            AbsaTemporaryDataSourceSingleton.jsessionId?.apply {
                addHeader("JSESSIONID", this)
            }

            AbsaTemporaryDataSourceSingleton.contentLength?.apply {
                val keyId = AbsaTemporaryDataSourceSingleton.keyId
                if (this > 0 && keyId?.isNotEmpty() == true) {
                    addHeader("x-encrypted", "$this|$keyId")
                }
            }

            AbsaTemporaryDataSourceSingleton.xEncryptedIv?.apply {
                this.toEncryptedHex()?.let { addHeader("x-encryption-iv", it) }
            }

            AbsaTemporaryDataSourceSingleton.xEncryptionKey?.apply {
                this.toEncryptedHex()?.let { addHeader("x-encryption-key", it) }
            }
        }
        proceed(requestBuilder.build())
    }
}