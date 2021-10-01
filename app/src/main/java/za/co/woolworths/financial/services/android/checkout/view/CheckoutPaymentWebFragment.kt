package za.co.woolworths.financial.services.android.checkout.view

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_checkout_payment_web.*
import okhttp3.OkHttpClient
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities
import android.webkit.WebResourceResponse

import com.google.common.net.HttpHeaders
import okhttp3.Request
import okhttp3.Response


class CheckoutPaymentWebFragment : Fragment(), WebViewCallbackListener {

    companion object {
        const val KEY_ARGS_WEB_TOKEN = "web_tokens"
        const val KEY_STATUS = "status"
    }

    private var headers = HashMap<String, String>()

    enum class PaymentStatus(val type: String) {
        PAYMENT_SUCCESS("success"),
        PAYMENT_ABANDON("abandon"),
        PAYMENT_UNAUTHENTICATED("unauthenticated")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? CheckoutActivity)?.apply {
            supportActionBar?.let {
                it.hide()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_checkout_payment_web, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPaymentWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initPaymentWebView() {
        clearWebCache()
        activity?.let {
            checkoutPaymentWebView?.apply {
                settings.javaScriptEnabled = true
                settings.useWideViewPort = true
                settings.loadsImagesAutomatically = true
                settings.builtInZoomControls = true; // allow pinch to zooom
                settings.displayZoomControls =
                    false; // disable the default zoom controls on the page

                webViewClient = CheckoutPaymentWebViewClient(this@CheckoutPaymentWebFragment)
//                webViewClient = CustomWebViewClient(this@CheckoutPaymentWebFragment)
                val paymentUrl = WoolworthsApplication.getNativeCheckout()?.checkoutPaymentURL ?: ""
                val webTokens =
                    arguments?.getSerializable(KEY_ARGS_WEB_TOKEN) as? ShippingDetailsResponse
                if (webTokens != null) {

                    val cookies = "TOKEN=${webTokens.jsessionId};AUTHENTICATION=${webTokens.auth}"
                    Log.d("EJ", "New Cookies >> $cookies")

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptThirdPartyCookies(this, true);
//                    cookieManager.setAcceptCookie(true)

                    cookies.split(";").forEach { item ->
                        cookieManager.setCookie("https://www-win-qa.woolworths.co.za/", item)
                        cookieManager.setCookie("http://www-win-qa.woolworths.co.za/", item)
                    }
                    cookieManager.flush()

                    val additionalHeaders: MutableMap<String, String> = java.util.HashMap()
                    additionalHeaders["Cookie"] = cookies
//                    additionalHeaders["Set-Cookie"] = cookies

                    loadUrl(paymentUrl, additionalHeaders)
                } else {
                    view?.findNavController()?.navigateUp()
                }
            }
        }
    }

    class CheckoutPaymentWebViewClient internal constructor(private val listener: WebViewCallbackListener): WebViewClient(){

        private var isPageReloadRequired = false

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            view?.evaluateJavascript("(function(){return document.cookie; })()") { cookie ->
                Log.d("EJ", "onPageStarted Document Cookie >> $cookie")
                if ("null".equals(cookie)){
                    isPageReloadRequired = true
                }
            }
            listener.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            view?.evaluateJavascript("(function(){return document.URL + document.cookie; })()") { cookie ->
                Log.d("EJ", "onPageFinished Document Cookie >> $cookie")
                if (!"null".equals(cookie) && isPageReloadRequired){
                    isPageReloadRequired = false
                    view?.reload()
                }
            }

            listener.onPageFinished(view, url)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            listener.onReceivedError(view,request, error)
        }
    }

    private fun clearWebCache() {
        checkoutPaymentWebView?.apply {
            clearCache(true)
            clearHistory()
            context?.let { clearCookies() }
        }
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeSessionCookies(null)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        progressBar?.visibility = View.VISIBLE
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        progressBar?.visibility = View.GONE
        when (val status = Uri.parse(url).getQueryParameter(KEY_STATUS)) {
            PaymentStatus.PAYMENT_SUCCESS.type -> {
                navigateToOrderConfirmation()
            }
            PaymentStatus.PAYMENT_UNAUTHENTICATED.type, PaymentStatus.PAYMENT_ABANDON.type -> {
                setFragmentResult(
                    "paymentStatus", bundleOf(
                        KEY_STATUS to status
                    )
                )
            }
        }
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                error.errorCode == 400
            } else {
                TODO("VERSION.SDK_INT < M")
            }
        ) {
            return
        }
    }

    override fun getHeaders(): MutableMap<String, String> {
        return headers;
    }

    private fun navigateToOrderConfirmation() {
        // TODO: Navitgate to order confirmation screen as payment is successful
    }
}