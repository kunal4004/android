package za.co.woolworths.financial.services.android.models.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import java.io.InputStreamReader

open class CompletionHandler<T>(private val requestListener: IResponseListener<T>?, private val typeParameterClass: Class<T>) : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        this.requestListener?.apply {
            response?.apply {
                if (displayMaintenanceScreenIfNeeded(this)) return
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> errorBody()?.apply {
                        if (response.code() == 504) {
                            onFailure(Throwable(this.string()))
                        } else {
                            try {
                                val stream = InputStreamReader(byteStream())
                                onSuccess(Gson().fromJson(stream, typeParameterClass))
                            } catch (exception: JsonSyntaxException) {
                                onFailure(Throwable(this.string()))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
        if (!call.isCanceled) {
            this.requestListener?.onFailure(throwable)
            RetrofitException(throwable).show()
        }
    }

    private fun displayMaintenanceScreenIfNeeded(response: Response<T>): Boolean = RetrofitException(response.code()).show()
}
