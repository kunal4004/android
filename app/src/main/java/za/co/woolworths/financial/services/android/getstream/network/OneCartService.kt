package za.co.woolworths.financial.services.android.getstream.network

import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import java.util.concurrent.TimeUnit

class OneCartService {

    var api: OneCartAPI
        private set

    companion object {
        var instance = OneCartService()
    }

    init {
        val httpClient: OkHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    val requestBuilder: Request.Builder = chain.request().newBuilder()
                    requestBuilder.header("Content-Type", "application/json")
                    requestBuilder.header("x-api-key", "OYAFDCA0gwzgzQMMk9ePTQ==") //TODO: retrieve API key from config
                    requestBuilder.header("Authorization", "DioZxoUt8u+DT/UR11nK3t407bghXhK5axq4FELIo6o=") //TODO: retrieve API secret from config
                    chain.proceed(requestBuilder.build())
                }.readTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
                .build()
        val baseUrl = "https://staging-sls-api.onecart.co.za" //TODO: retrieve this from config
        val retrofit = Retrofit.Builder().baseUrl(baseUrl).client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        api = retrofit.create(OneCartAPI::class.java)
    }
}