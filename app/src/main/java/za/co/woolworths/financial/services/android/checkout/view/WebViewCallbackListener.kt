package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView

interface WebViewCallbackListener {
    fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
    fun onPageFinished(view: WebView?, url: String?)
    fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError)
    fun getHeaders(): MutableMap<String, String>
}