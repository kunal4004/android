package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.util.Log
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class PayMyAccountService {

    fun getPayUMethod() {
        request(OneAppService.queryServicePayUMethod(), object : IGenericAPILoaderView<Any> {
            override fun onSuccess(response: Any?) {
                Log.e("responseList", Gson().toJson(response))
            }
        })
    }

}