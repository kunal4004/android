package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.util.Log
import android.webkit.JavascriptInterface
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IPayUInterface
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse

class PayUCardFormJavascriptBridge(private val payU: IPayUInterface?) {

    @JavascriptInterface
    fun showMessageInNative(token: String) {
        Log.e("showMessageInNative",token)
        val addToCardResponse = Gson().fromJson<AddCardResponse>(token, AddCardResponse::class.java)
        payU?.onAddNewCardSuccess(addToCardResponse)
    }

    @JavascriptInterface
    fun showInProgress() {
        Log.e("payUCardForm", "showProgress")
        payU?.onAddCardProgressStarted()
    }

    @JavascriptInterface
    fun onFailureHandler() {
        Log.e("payUCardForm", "onFailureHandler")
        payU?.onAddCardFailureHandler()
    }

}