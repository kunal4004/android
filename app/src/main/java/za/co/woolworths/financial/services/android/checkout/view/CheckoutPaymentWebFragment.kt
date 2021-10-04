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
import android.text.TextUtils
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
import za.co.woolworths.financial.services.android.util.AdvancedWebView
import java.net.URI


class CheckoutPaymentWebFragment : Fragment(), AdvancedWebView.Listener {

    companion object {
        const val KEY_ARGS_WEB_TOKEN = "web_tokens"
        const val KEY_STATUS = "status"
    }

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
        checkoutPaymentWebView?.apply {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().removeSessionCookies {
            }
            CookieManager.getInstance().flush()

            CookieManager.getInstance().acceptCookie()
            val paymentUrl = WoolworthsApplication.getNativeCheckout()?.checkoutPaymentURL
            val webTokens = arguments?.getSerializable(KEY_ARGS_WEB_TOKEN) as? ShippingDetailsResponse
            val cookie = "TOKEN=${webTokens?.jsessionId};AUTHENTICATION=${webTokens?.auth};"

            if(TextUtils.isEmpty(paymentUrl) || TextUtils.isEmpty(webTokens?.jsessionId)
                || TextUtils.isEmpty(webTokens?.auth)){
                return@apply
            }

            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            val cookieManager = CookieManager.getInstance()

            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            cookie.split(";").forEach { item ->
                cookieManager.setCookie(URI.create(paymentUrl).host, item)
            }
            cookieManager.flush()

            setListener(activity as? CheckoutActivity, this@CheckoutPaymentWebFragment)
            paymentUrl?.let { loadUrl(it) }
        }
    }

    private fun navigateToOrderConfirmation() {
        // TODO: Navitgate to order confirmation screen as payment is successful
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        progressBar?.visibility = View.VISIBLE
        if(Uri.parse(url).getQueryParameter(KEY_STATUS) == PaymentStatus.PAYMENT_ABANDON.type){
            view?.findNavController()?.navigateUp()
        }
    }

    override fun onPageFinished(url: String?) {
        progressBar?.visibility = View.GONE
        when (Uri.parse(url).getQueryParameter(KEY_STATUS)) {
            PaymentStatus.PAYMENT_SUCCESS.type -> {
                navigateToOrderConfirmation()
            }
            PaymentStatus.PAYMENT_UNAUTHENTICATED.type, PaymentStatus.PAYMENT_ABANDON.type -> {
                view?.findNavController()?.navigateUp()
            }
        }
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?
    ) {
    }

    override fun onExternalPageRequest(url: String?) {
    }
}