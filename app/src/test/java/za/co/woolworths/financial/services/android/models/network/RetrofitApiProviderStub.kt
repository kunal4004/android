package za.co.woolworths.financial.services.android.models.network

import okhttp3.OkHttpClient
import org.mockito.Mockito

class RetrofitApiProviderStub: RetrofitApiProviderInterface {
    override fun getRetrofit(httpBuilder: OkHttpClient.Builder): ApiInterface {
        return Mockito.mock(ApiInterface::class.java)
    }
}