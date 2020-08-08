package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.webkit.JavascriptInterface
import za.co.woolworths.financial.services.android.contracts.IPayUInterface

/**
 * Receive message from webview and pass on to native.
 */
class PayUCardFormJavascriptBridge(private val payU: IPayUInterface?) {

    @JavascriptInterface
    fun showMessageInNative(message: String) {
        payU?.onAddNewCardSuccess(message)
    }
}