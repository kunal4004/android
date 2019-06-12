package za.co.woolworths.financial.services.android.models.network

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.ui.activities.maintenance.MaintenanceMessageViewController
import za.co.woolworths.financial.services.android.ui.extension.fromJson

open class CompletionHandler<Result>(protected var listener: RequestListener<Result>?) : Callback<Result> {

    private val jsonMimeTypes = arrayListOf("application/json", "application/json; charset=utf-8")

    override fun onResponse(call: Call<Result>, response: Response<Result>?) {
        this.listener?.apply {
            response?.apply {
                if (displayMaintenanceScreenIfNeeded(this)) return
                when (isSuccessful) {
                    true -> onSuccess(body())
                    else -> errorBody()?.apply {
                        when (jsonMimeTypes.contains(contentType()?.subtype())) {
                            true -> {
                                string().apply {
                                    val result: Class<Result> = Gson().fromJson(this)
                                    //onSuccess(result.cast())
                                }
                            }
                            else -> {
                                displayMaintenanceScreenIfNeeded(response)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<Result>, t: Throwable) {
        this.listener?.onFailure(t)
    }

    private fun displayMaintenanceScreenIfNeeded(response: Response<Result>): Boolean {
        if (response.code() == 404 || response.code() == 503) {
            val maintenanceMessageViewController = MaintenanceMessageViewController(MaintenanceMessageViewController::class.java.simpleName)
            maintenanceMessageViewController.openActivity()
            return true
        }
        return false
    }
}
