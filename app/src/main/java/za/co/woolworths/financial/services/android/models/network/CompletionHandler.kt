package za.co.woolworths.financial.services.android.models.network

import android.annotation.SuppressLint
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.RequestListener
import java.io.InputStreamReader

open class CompletionHandler<ResponseObj>(private val requestListener: RequestListener<ResponseObj>?, private val genericClassType: Class<ResponseObj>) : Callback<ResponseObj> {

    private val jsonMimeTypes = arrayListOf("application/json", "application/json; charset=utf-8")

    @SuppressLint("DefaultLocale")
    override fun onResponse(call: Call<ResponseObj>, response: Response<ResponseObj>?) {
        this.requestListener?.apply {
            response?.apply {
                if (displayMaintenanceScreenIfNeeded(this)) return
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> errorBody()?.apply {
                        val mimeType = contentType()?.type() + "/" + contentType()?.subtype()
                        val charsetMimType = mimeType + "; charset=" + contentType()?.charset()
                        when (jsonMimeTypes.contains(mimeType.toLowerCase()) || jsonMimeTypes.contains(charsetMimType.toLowerCase())) {
                            true -> onSuccess(Gson().fromJson(InputStreamReader(byteStream()), genericClassType))
                            else -> displayMaintenanceScreenIfNeeded(response)
                        }
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<ResponseObj>, throwable: Throwable) {
        this.requestListener?.onFailure(throwable)
        RetrofitException(throwable).show()
    }

    private fun displayMaintenanceScreenIfNeeded(response: Response<ResponseObj>): Boolean = RetrofitException(response.code()).show()
}
