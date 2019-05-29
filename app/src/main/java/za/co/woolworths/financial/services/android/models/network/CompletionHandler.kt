package za.co.woolworths.financial.services.android.models.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.RequestListener
import java.io.InputStreamReader

open class CompletionHandler<T>(protected var listener: RequestListener<T>?) : Callback<T> {

    val jsonMimeTypes = arrayListOf("application/json", "application/json; charset=utf-8")

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        this.listener?.apply {
            response?.apply {
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> {
                        val stream = InputStreamReader(errorBody()?.byteStream())
                        val result = Gson().fromJson<T>(stream, object : TypeToken<T>() {}.type)
                        onSuccess(result)
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        this.listener?.onFailure(t)
    }
}
