package za.co.woolworths.financial.services.android.checkout.view

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
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities

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
                webViewClient = CustomWebViewClient(this@CheckoutPaymentWebFragment)
                val paymentUrl = WoolworthsApplication.getNativeCheckout()?.checkoutPaymentURL ?: ""
                val webTokens =
                    arguments?.getSerializable(KEY_ARGS_WEB_TOKEN) as? ShippingDetailsResponse
                if (webTokens != null) {
                    val cookieValue =
                        "TOKEN=${webTokens.jsessionId};AUTHENTICATION=${webTokens.auth};"
                    /*CookieManager.getInstance()
                        .setCookie(paymentUrl, "TOKEN=${webTokens.jsessionId}")
                    CookieManager.getInstance()
                        .setCookie(paymentUrl, "AUTHENTICATION=${webTokens.auth}")
                    CookieManager.getInstance().setAcceptCookie(true)
*/
                    /*headers = hashMapOf(
                        "Cookie" to cookieValue
                    )*/

                    val extraHeaders: MutableMap<String, String> = java.util.HashMap()
                    extraHeaders["Cookie"] = cookieValue

                    loadUrl(
                        paymentUrl, extraHeaders
                    )
                } else {
                    view?.findNavController()?.navigateUp()
                }
            }
        }
    }

    class CustomWebViewClient internal constructor(private val listener: WebViewCallbackListener) :
        WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            listener.onPageStarted(view, url, favicon)
            Log.e("nikesh", "Url >> $url")
            Log.e("nikesh", "Url >> ${listener.getHeaders()}")
            view?.loadUrl("javascript:console.log('nikesh >> start')")
            view?.loadUrl("javascript:console.log(document.cookie)")
            val cookie = "TOKEN=K1Uw_pzuEmLfYS0fx7d9FDxf2r0TZeWR99Xemf5Yk2Uob8gAs7Ti!-1723856491;"
            val cookie2 = "AUTHENTICATION=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uSWQiOiJLMVV3X3B6dUVtTGZZUzBmeDdkOUZEeGYycjBUWmVXUjk5WGVtZjVZazJVb2I4Z0FzN1RpIS0xNzIzODU2NDkxIiwiaWF0IjoxNjMyOTA5NTk4LCJleHAiOjE2NjQ0NDU1OTh9.IwfUbolF976MBxF9C_eXNYkQy1UhRswu992-F3hmngM; f5avraaaaaaaaaaaaaaaa_session_=CALFLAELPEIALAMPNJICHIGFNNEHHEBOJDEPLNKMKBNPEOJEGAPONMECECBFILMLPAJDOBKNDAJAJLENGPHADBBBMENAJOAGMFLICFEMGBMKPFCMKEPIBELFGNLOIIGO; f5_cspm=1234; mt.v=2.132404388.1632911668614; mt.sc=%7B%22i%22%3A1632911673879%2C%22d%22%3A%5B%5D%7D; _gcl_au=1.1.662607130.1632911674; SearchCookie=0AJzL1632911676564hgNKLs; _hjid=0c8e3bc6-fc76-44cf-ad76-86673ee4eaa4; _hjFirstSeen=1; TS01b7ced4=01f08501b08ff44d7afaad3cb094f2eb44810f22fe105a64b47a2d7c22afb4804ef9756a2aea9ce20c387678b5df1d58cbca3219d56d8435dbf1b604b32a91accd374db964bbe3e7fe55140353453768be8b1eb7f01517b7cdb0709813204581129a06af8cdb1a4497071e49e6cccef65b8f0b789c;"
            view?.loadUrl("javascript:document.cookie = '${cookie}'")
            view?.loadUrl("javascript:document.cookie = '${cookie2}'")
            view?.loadUrl("javascript:console.log(document.cookie)")
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            view?.loadUrl("javascript:console.log(document.cookie)")
            val cookie = "TOKEN=K1Uw_pzuEmLfYS0fx7d9FDxf2r0TZeWR99Xemf5Yk2Uob8gAs7Ti!-1723856491; AUTHENTICATION=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXNzaW9uSWQiOiJLMVV3X3B6dUVtTGZZUzBmeDdkOUZEeGYycjBUWmVXUjk5WGVtZjVZazJVb2I4Z0FzN1RpIS0xNzIzODU2NDkxIiwiaWF0IjoxNjMyOTA5NTk4LCJleHAiOjE2NjQ0NDU1OTh9.IwfUbolF976MBxF9C_eXNYkQy1UhRswu992-F3hmngM; f5avraaaaaaaaaaaaaaaa_session_=CALFLAELPEIALAMPNJICHIGFNNEHHEBOJDEPLNKMKBNPEOJEGAPONMECECBFILMLPAJDOBKNDAJAJLENGPHADBBBMENAJOAGMFLICFEMGBMKPFCMKEPIBELFGNLOIIGO; f5_cspm=1234; mt.v=2.132404388.1632911668614; mt.sc=%7B%22i%22%3A1632911673879%2C%22d%22%3A%5B%5D%7D; _gcl_au=1.1.662607130.1632911674; SearchCookie=0AJzL1632911676564hgNKLs; _hjid=0c8e3bc6-fc76-44cf-ad76-86673ee4eaa4; _hjFirstSeen=1; TS01b7ced4=01f08501b08ff44d7afaad3cb094f2eb44810f22fe105a64b47a2d7c22afb4804ef9756a2aea9ce20c387678b5df1d58cbca3219d56d8435dbf1b604b32a91accd374db964bbe3e7fe55140353453768be8b1eb7f01517b7cdb0709813204581129a06af8cdb1a4497071e49e6cccef65b8f0b789c"
            view?.loadUrl("javascript:document.cookie = '${cookie}'")
            view?.loadUrl("javascript:console.log(document.cookie)")
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url: String = request?.url.toString();
            view?.loadUrl(url, listener.getHeaders())
            Log.e("nikesh", "Url >> $url")
            Log.e("nikesh", "Url >> ${listener.getHeaders()}")
            view?.loadUrl("javascript:alert(document.cookie)")

            return true
        }

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url, listener.getHeaders())
            Log.e("nikesh", "Url >> $url")
            Log.e("nikesh", "Url >> ${listener.getHeaders()}")
            webView?.loadUrl("javascript:console.log('nikesh')")

            return true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            listener.onReceivedError(view, request, error)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.loadUrl("javascript:console.log('nikesh >> start')")

            listener.onPageFinished(view, url)
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