package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.di


import android.content.Context
import android.net.NetworkRequest
import com.awfs.coordination.BuildConfig
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
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.models.network.WfsApiInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import za.co.woolworths.financial.services.android.models.network.CommonHeaderUtils
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import android.net.*
import android.os.Build
import androidx.lifecycle.LiveData
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

//TODO:: delete class and edit imports once Store card enhancements merged to dev
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideInternetConnectionCheck(@ApplicationContext context: Context) = ConnectivityLiveData(context)


    @Singleton
    @Provides
    fun provideRetrofit( httpBuilder: OkHttpClient.Builder) : Retrofit  = Retrofit.Builder()
        .baseUrl("${BuildConfig.HOST}/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .client(httpBuilder.build())
        .build()

    @Provides
    fun provideHttpBuilder(logging: HttpLoggingInterceptor) :OkHttpClient.Builder = OkHttpClient.Builder().apply {
        addInterceptor(WfsApiInterceptor())
        addInterceptor(CommonHeaderInterceptor())
        readTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
        connectTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
        writeTimeout(if (BuildConfig.ENV.equals("qa", true)) RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT_QA else RetrofitConfig.READ_CONNECT_TIMEOUT_UNIT, TimeUnit.SECONDS)
        interceptors().add(logging)
    }

    @Provides
    fun provideInterceptor(@ApplicationContext appContext: Context): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (Util.isDebug(appContext)) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        return logging
    }

}

class CommonHeaderInterceptor : NetworkConfig(), Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
            .newBuilder()
            .addHeader(CommonHeaderUtils.API_ID, getApiId())
            .addHeader(CommonHeaderUtils.SHA1_PASSWORD, getSha1Password())
            .addHeader(CommonHeaderUtils.DEVICE_VERSION, getDeviceManufacturer())
            .addHeader(CommonHeaderUtils.DEVICE_MODEL, getDeviceModel())
            .addHeader(CommonHeaderUtils.NETWORK, getNetworkCarrier())
            .addHeader(CommonHeaderUtils.OS, getOS())
            .addHeader(OS_VERSION, getOsVersion())
            .addHeader(USER_AGENT, "")
            .addHeader(USER_AGENT_VERSION, "")
            .addHeader(OS_VERSION, getOsVersion())
            .addHeader(SESSION_TOKEN, getSessionToken())
        return chain.proceed(request.build())
    }
}
const val OS_VERSION = "osVersion"
const val  USER_AGENT = "userAgent"
const val USER_AGENT_VERSION = "userAgentVersion"
const val SESSION_TOKEN = "sessionToken"
const val DEVICE_IDENTITY_TOKEN = "deviceIdentityToken"
const val APP_VERSION="appversion"

class ConnectivityLiveData @Inject constructor(context: Context) : LiveData<Boolean>() {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager

    override fun onActive() {
        super.onActive()
        val networkRequestBuilder = getNetworkCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(networkRequestBuilder)
        } else {
            connectivityManager?.registerNetworkCallback(getNetworkRequest(), networkRequestBuilder)
        }
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager?.unregisterNetworkCallback(getNetworkCallback())
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
    }

    private fun getNetworkRequest(): NetworkRequest {
        val networkRequestBuilder = NetworkRequest.Builder()
        with(networkRequestBuilder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
        }
        return networkRequestBuilder.build()
    }

    private fun getNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

}