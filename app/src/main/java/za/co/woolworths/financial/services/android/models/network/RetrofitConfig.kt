package za.co.woolworths.financial.services.android.models.network

import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import za.co.wigroup.androidutils.Util
import java.util.concurrent.TimeUnit

abstract class RetrofitConfig(appContextProvider: AppContextProviderInterface, apiInterfaceProvider: RetrofitApiProviderInterface) : NetworkConfig(appContextProvider) {

    companion object {
        const val READ_CONNECT_TIMEOUT_UNIT: Long = 45
        const val READ_CONNECT_TIMEOUT_UNIT_QA: Long = 180
        lateinit var mApiInterface: ApiInterface
    }

    init {
        val httpBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()

        logging.level = if (Util.isDebug(appContextProvider.appContext())) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        with(httpBuilder) {
            addInterceptor(WfsApiInterceptor(NetworkConfig(AppContextProviderImpl())))
            addInterceptor(CommonHeaderInterceptor())
            readTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            connectTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            writeTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            interceptors().add(logging)
        }
        mApiInterface = apiInterfaceProvider.getRetrofit(httpBuilder)
    }

    fun cancelRequest(call: Call<*>?) = call?.apply { if (!isCanceled) cancel() }
}