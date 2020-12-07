package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse

class PayUCardFormJavascriptBridge(val onShowInProgress: () -> Unit,
                                   val onShowMessageInNative: (addToCardResponse: AddCardResponse) -> Unit,
                                   val onFailure: () -> Unit,val onPayUFormLoaded : () -> Unit) {

    @JavascriptInterface
    fun showMessageInNative(token: String) {
        onShowMessageInNative(Gson().fromJson(token, AddCardResponse::class.java))
    }

    @JavascriptInterface
    fun showInProgress() {
        onShowInProgress()
    }

    @JavascriptInterface
    fun onFailureHandler() {
        onFailure()
    }

    @JavascriptInterface
    fun payuFormHasLoaded(){
        onPayUFormLoaded()
    }

}