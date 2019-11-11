package za.co.woolworths.financial.services.android.models.network

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.RequestListener
import java.io.InputStreamReader

open class CompletionHandler<ResponseObj>(private val requestListener: RequestListener<ResponseObj>?, private val genericClassType: Class<ResponseObj>) : Callback<ResponseObj> {

    override fun onResponse(call: Call<ResponseObj>, response: Response<ResponseObj>?) {
        this.requestListener?.apply {
            response?.apply {
                if (displayMaintenanceScreenIfNeeded(this)) return
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> errorBody()?.apply { onSuccess(Gson().fromJson(InputStreamReader(byteStream()), genericClassType)) }
                }
            }
        }
    }

    override fun onFailure(call: Call<ResponseObj>, throwable: Throwable) {
        if (!call.isCanceled) {
            this.requestListener?.onFailure(throwable)
            RetrofitException(throwable).show()
        }
    }

    private fun displayMaintenanceScreenIfNeeded(response: Response<ResponseObj>): Boolean = RetrofitException(response.code()).show()
}
