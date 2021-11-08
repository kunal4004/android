package za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import java.io.IOException

class ReceivedCookiesInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        val cookies = mutableListOf<String>()
        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies.add(header)
            }

            if (cookies.size > 0){
                val cookie = cookies[0]
                AbsaTemporaryDataSourceSingleton.cookie = cookie
                AbsaTemporaryDataSourceSingleton.jsessionId = cookie.replace("JSESSIONID=", "")
                }
            }
        return originalResponse
    }
}