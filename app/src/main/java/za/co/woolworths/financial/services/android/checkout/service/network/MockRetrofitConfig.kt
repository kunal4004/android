package za.co.woolworths.financial.services.android.checkout.service.network

import androidx.annotation.VisibleForTesting
import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.WfsApiInterceptor
import java.util.concurrent.TimeUnit

/**
 * Created by Kunal Uttarwar on 10/06/21.
 */
abstract class MockRetrofitConfig : NetworkConfig() {

    companion object {
        private const val READ_CONNECT_TIMEOUT_UNIT: Long = 45
        private const val READ_CONNECT_TIMEOUT_UNIT_QA: Long = 180
        lateinit var mockApiInterface: ApiInterface
    }

    init {
        val httpBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()

        logging.level = if (Util.isDebug(appContext())) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        with(httpBuilder) {
            addInterceptor(WfsApiInterceptor())
            readTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            connectTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            writeTimeout(if (BuildConfig.ENV.equals("qa", true)) READ_CONNECT_TIMEOUT_UNIT_QA else READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            interceptors().add(logging)
        }

        mockApiInterface = getRetrofit(httpBuilder)
    }

    private fun getRetrofit(httpBuilder: OkHttpClient.Builder): ApiInterface {
        return Retrofit.Builder()
        .baseUrl("https://8575ba83-72eb-4d28-9d4c-11ae953ea601.mock.pstmn.io/" + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpBuilder.build())
            .build()
            .create(ApiInterface::class.java)
    }

    fun cancelRequest(call: Call<*>?) = call?.apply { if (!isCanceled) cancel() }

    @VisibleForTesting
    fun testGetRetrofit(httpBuilder: OkHttpClient.Builder): ApiInterface {
        return getRetrofit(httpBuilder)
    }
}