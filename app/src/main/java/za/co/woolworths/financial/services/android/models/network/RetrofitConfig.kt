package za.co.woolworths.financial.services.android.models.network

import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.wigroup.androidutils.Util
import java.util.concurrent.TimeUnit

abstract class RetrofitConfig : NetworkConfig() {

    companion object {
        private const val READ_CONNECT_TIMEOUT_UNIT: Long = 45
        lateinit var mApiInterface: ApiInterface
    }

    init {
        val httpBuilder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()

        logging.level = if (Util.isDebug(appContext())) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

        httpBuilder.apply {
            addInterceptor(WfsApiInterceptor())
            readTimeout(READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            connectTimeout(READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            interceptors().add(logging)
        }

        mApiInterface = Retrofit.Builder()
                .baseUrl(BuildConfig.HOST + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpBuilder.build())
                .build()
                .create(ApiInterface::class.java)
    }
}