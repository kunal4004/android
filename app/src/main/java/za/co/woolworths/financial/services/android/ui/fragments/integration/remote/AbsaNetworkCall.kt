package za.co.woolworths.financial.services.android.ui.fragments.integration.remote

import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.models.network.CommonHeaderInterceptor
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig.Companion.READ_CONNECT_TIMEOUT_UNIT
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig.Companion.READ_CONNECT_TIMEOUT_UNIT_QA
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor.CommonAbsaHeadersInterceptor
import za.co.woolworths.financial.services.android.ui.fragments.integration.remote.interceptor.ReceivedCookiesInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AbsaNetworkCall @Inject constructor() : IAbsaNetworkCall {

    override fun <T> build(clazz: Class<T>, baseUrl: String): T {
        val httpBuilder = okHttpClientBuilder()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpBuilder.build())
            .build()
            .create(clazz)
    }

    override fun okHttpClientBuilder(): OkHttpClient.Builder {
        val httpBuilder = OkHttpClient.Builder()
        with(httpBuilder) {
            addHeaderInterceptor(this)
            addCookieInterceptor(this)
            addHttpLoggingInterceptor(this)
        }
        return httpBuilder
    }

    override fun addHeaderInterceptor(httpBuilder: OkHttpClient.Builder) {
        httpBuilder.addInterceptor(CommonAbsaHeadersInterceptor())
        httpBuilder.addInterceptor(CommonHeaderInterceptor())
    }

    override fun addCookieInterceptor(httpBuilder: OkHttpClient.Builder) {
        httpBuilder.addInterceptor(ReceivedCookiesInterceptor())
    }

    override fun addHttpLoggingInterceptor(httpBuilder: OkHttpClient.Builder) {
        val logging = HttpLoggingInterceptor()
        logging.level =
            if (Util.isDebug(WoolworthsApplication.getAppContext())) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        with(httpBuilder) {
            if (BuildConfig.ENV.equals("qa", true)) {
                readTimeout(READ_CONNECT_TIMEOUT_UNIT_QA, TimeUnit.SECONDS)
                connectTimeout(READ_CONNECT_TIMEOUT_UNIT_QA, TimeUnit.SECONDS)
                writeTimeout(READ_CONNECT_TIMEOUT_UNIT_QA, TimeUnit.SECONDS)
            } else {
                readTimeout(READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
                connectTimeout(READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
                writeTimeout(READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            }
            interceptors().add(logging)
        }
    }
}

/** Globally access the Retrofit method(s) */
object AbsaRemoteApi {
    val service: AbsaApi by lazy {
        AbsaNetworkCall().build(AbsaApi::class.java, "${BuildConfig.HOST}/creditcard-service/app/v4/" )
    }
}

object RemoteDataSource {
    val service: ApiInterface by lazy {
        AbsaNetworkCall().build(ApiInterface::class.java, BuildConfig.HOST + "/" )
    }
}

