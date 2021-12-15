package za.co.woolworths.financial.services.android.ui.fragments.integration.remote

import okhttp3.OkHttpClient

interface IAbsaNetworkCall {
    fun <T> build(clazz: Class<T>,baseUrl : String): T
    fun okHttpClientBuilder(): OkHttpClient.Builder
    fun addHeaderInterceptor(httpBuilder: OkHttpClient.Builder)
    fun addHttpLoggingInterceptor(httpBuilder: OkHttpClient.Builder)
    fun addCookieInterceptor(httpBuilder: OkHttpClient.Builder)
}