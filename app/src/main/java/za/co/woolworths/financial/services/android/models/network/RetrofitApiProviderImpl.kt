package za.co.woolworths.financial.services.android.models.network

import com.awfs.coordination.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitApiProviderImpl : RetrofitApiProviderInterface {
    override fun getRetrofit(httpBuilder: OkHttpClient.Builder): ApiInterface {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.HOST + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpBuilder.build())
            .build()
            .create(ApiInterface::class.java)
    }
}