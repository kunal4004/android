package za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.ABSA_COOKIE_WFPT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.ABSA_COOKIE_XFPT
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

                for (cookie in cookies){
                    when {
                        cookie.lowercase().startsWith(ABSA_COOKIE_XFPT) -> AbsaTemporaryDataSourceSingleton.wfpt = cookie
                        cookie.lowercase().startsWith(ABSA_COOKIE_WFPT) -> AbsaTemporaryDataSourceSingleton.xfpt = cookie
                    }
                }

                val cookie = cookies[0]
                AbsaTemporaryDataSourceSingleton.cookie = cookie
                AbsaTemporaryDataSourceSingleton.jsessionId = cookie.replace("JSESSIONID=", "")
                }
            }
        return originalResponse
    }
}