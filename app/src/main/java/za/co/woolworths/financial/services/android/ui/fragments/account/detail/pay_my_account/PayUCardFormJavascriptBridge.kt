package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse

class PayUCardFormJavascriptBridge(val onShowInProgress : () -> Unit,
                                   val onShowMessageInNative : (addToCardResponse: AddCardResponse) -> Unit,
                                   val onFailure : () -> Unit) {

    @JavascriptInterface
    fun showMessageInNative(token: String) {
        val addToCardResponse = Gson().fromJson<AddCardResponse>(token, AddCardResponse::class.java)
        onShowMessageInNative(addToCardResponse)
    }

    @JavascriptInterface
    fun showInProgress() {
        onShowInProgress()
    }

    @JavascriptInterface
    fun onFailureHandler() {
        onFailure()
    }

}