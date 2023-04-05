package za.co.woolworths.financial.services.android.models.network

import okhttp3.Request
import okio.Timeout
import org.mockito.Mockito.mock
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallVoidStub: Call<Void> {
    override fun clone(): Call<Void> = CallVoidStub()

    override fun execute(): Response<Void> = Response.success(null)

    override fun enqueue(callback: Callback<Void>) {}

    override fun isExecuted(): Boolean = false

    override fun cancel() {}

    override fun isCanceled(): Boolean = false

    override fun request(): Request = mock(Request::class.java)

    override fun timeout(): Timeout = mock(Timeout::class.java)
}