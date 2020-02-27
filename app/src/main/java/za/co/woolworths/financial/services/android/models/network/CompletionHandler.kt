package za.co.woolworths.financial.services.android.models.network

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.RequestListener
import java.io.InputStreamReader

open class CompletionHandler<T>(private val requestListener: RequestListener<T>?, private val typeParameterClass: Class<T>) : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        this.requestListener?.apply {
            response?.apply {
                if (displayMaintenanceScreenIfNeeded(this)) return
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> errorBody()?.apply {
                        if (response.code() == 504){
                            onFailure(Throwable(this.string()));
                        } else{
                            val stream = InputStreamReader(byteStream());
                            onSuccess(Gson().fromJson(stream, typeParameterClass));
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
