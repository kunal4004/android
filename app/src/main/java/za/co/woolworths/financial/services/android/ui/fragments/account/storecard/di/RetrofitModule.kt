package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.di

import android.content.Context
import com.awfs.coordination.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.network.CommonHeaderInterceptor
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.models.network.WfsApiInterceptor
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {
    @Singleton
    @Provides
    fun provideRetrofit( httpBuilder: OkHttpClient.Builder) : Retrofit  = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST + "/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(httpBuilder.build())
        .build()

    @Provides
    fun provideHttpBuilder(logging: HttpLoggingInterceptor) :OkHttpClient.Builder{
        val httpBuilder = OkHttpClient.Builder()
        with(httpBuilder) {
            addInterceptor(WfsApiInterceptor())
            addInterceptor(CommonHeaderInterceptor())
            readTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            connectTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            writeTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
            interceptors().add(logging)
        }
        return httpBuilder
    }

    @Provides
    fun provideInterceptor(@ApplicationContext appContext: Context): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (Util.isDebug(appContext)) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }
}