package za.co.woolworths.financial.services.android.checkout.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCheckoutPaymentWebBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.checkout.service.network.PaymentAnalyticsData
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.PAYMENT_STATUS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.STATUS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.TRANSACTION_ID
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyValues.Companion.CURRENCY_VALUE
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.util.AdvancedWebView
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import java.net.URI

class CheckoutPaymentWebFragment : Fragment(R.layout.fragment_checkout_payment_web),
    AdvancedWebView.Listener {

    companion object {
        const val KEY_ARGS_WEB_TOKEN = "web_tokens"
        const val KEY_STATUS = "status"
        const val REQUEST_KEY_PAYMENT_STATUS = "payment_status"
        const val PAYMENT_TYPE = "payment_type"
        const val PAYMENT_VALUE = "value"

    }

    enum class PaymentStatus(val type: String) {
        PAYMENT_SUCCESS("success"),
        PAYMENT_ABANDON("abandon"),
        PAYMENT_UNAUTHENTICATED("unauthenticated"),
        PAYMENT_ERROR("error")
    }

    private lateinit var binding: FragmentCheckoutPaymentWebBinding
    private var currentSuccessURI = ""
    private var cartItemList: ArrayList<CommerceItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? CheckoutActivity)?.apply {
            supportActionBar?.hide()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckoutPaymentWebBinding.bind(view)
        cartItemList =
            checkNotNull(arguments?.getSerializable(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST) as ArrayList<CommerceItem>?)
        initPaymentWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initPaymentWebView() {
        binding.checkoutPaymentWebView?.apply {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().removeSessionCookies {
            }
            CookieManager.getInstance().flush()

            CookieManager.getInstance().acceptCookie()
            val paymentUrl = AppConfigSingleton.nativeCheckout?.checkoutPaymentURL
            val webTokens =
                arguments?.getSerializable(KEY_ARGS_WEB_TOKEN) as? ShippingDetailsResponse
            val cookie = "TOKEN=${webTokens?.jsessionId};AUTHENTICATION=${webTokens?.auth};"

            if (TextUtils.isEmpty(paymentUrl) || TextUtils.isEmpty(webTokens?.jsessionId)
                || TextUtils.isEmpty(webTokens?.auth)
            ) {
                return@apply
            }

            webViewClient = CheckoutPaymentWebViewClient()
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

    inner class CheckoutPaymentWebViewClient : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            url?.let { onStatusChanged(it) }
        }
    }

    private fun navigateToOrderConfirmation() {
        binding.paymentSuccessConfirmationLayout?.root?.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.paymentSuccessConfirmationLayout?.root?.visibility = View.GONE
            view?.let {
                GeoUtils.navigateSafe(it, R.id.action_checkoutPaymentWebFragment_orderConfirmationFragment, null)
            }
        }, AppConstant.DELAY_1500_MS)
    }

    override fun onPageStarted(url: String?, favicon: Bitmap?) {
        binding.progressBar?.visibility = View.VISIBLE
    }

    override fun onPageFinished(url: String?) {
        binding.progressBar?.visibility = View.GONE
        url?.let { onStatusChanged(it) }
    }

    private fun onStatusChanged(url: String) {
        val uri = Uri.parse(url)
        currentSuccessURI = url
        val paymentStatusType = uri.getQueryParameter(KEY_STATUS)
        val transactionAnalytics = uri.getQueryParameter("analytics")
        val paymentArguments = HashMap<String, String>()
        if (!paymentStatusType.isNullOrEmpty())
            paymentArguments[STATUS] = paymentStatusType

        if (!transactionAnalytics.isNullOrEmpty()) {
            val jsonToAnalyticsList = Gson().fromJson<PaymentAnalyticsData?>(
                transactionAnalytics,
                object : TypeToken<PaymentAnalyticsData?>() {}.type
            )
            if (jsonToAnalyticsList != null)
                paymentArguments[TRANSACTION_ID] = jsonToAnalyticsList?.transaction_id ?: ""
        }

        when (paymentStatusType) {
            PaymentStatus.PAYMENT_SUCCESS.type -> {
                val selectItem = Bundle()
                val paymentType = uri.getQueryParameter(PAYMENT_TYPE)
                val paymentValue = uri.getQueryParameter(PAYMENT_VALUE)
                cartItemList?.let {
                    for (cartItem in it) {
                        selectItem.putString(
                            FirebaseAnalytics.Param.ITEM_ID,
                            cartItem.commerceItemInfo.productId
                        )

                        selectItem.putString(
                            FirebaseAnalytics.Param.ITEM_NAME,
                            cartItem.commerceItemInfo.productDisplayName
                        )

                        selectItem.putDouble(
                            FirebaseAnalytics.Param.PRICE,
                            cartItem.priceInfo.amount
                        )

                        selectItem.putString(
                            FirebaseAnalytics.Param.ITEM_BRAND,
                            cartItem.commerceItemInfo?.productDisplayName
                        )
                        selectItem.putString(
                            FirebaseAnalytics.Param.ITEM_VARIANT,
                            cartItem.commerceItemInfo?.size
                        )

                        selectItem.putString(
                            FirebaseAnalytics.Param.ITEM_CATEGORY,
                            cartItem.commerceItemInfo.productDisplayName
                        )
                        selectItem.putInt(
                            FirebaseAnalytics.Param.QUANTITY,
                            cartItem.commerceItemInfo.quantity
                        )

                        selectItem.putString(
                            FirebaseAnalytics.Param.CURRENCY,
                            CURRENCY_VALUE
                        )
                        selectItem.putString(
                            FirebaseAnalytics.Param.PAYMENT_TYPE,
                            paymentType.toString()
                        )
                        selectItem.putString(
                            FirebaseAnalytics.Param.VALUE,
                            paymentValue.toString()
                        )


                        selectItem.putParcelableArray(
                            FirebaseAnalytics.Param.ITEMS,
                            arrayOf(selectItem)
                        )
                    }
                }

                AnalyticsManager.logEvent(
                    FirebaseManagerAnalyticsProperties.ADD_PAYMENT_INFO,
                    selectItem
                )
                navigateToOrderConfirmation()
            }
            PaymentStatus.PAYMENT_ABANDON.type -> {
                view?.findNavController()?.navigateUp()
            }
            PaymentStatus.PAYMENT_UNAUTHENTICATED.type, PaymentStatus.PAYMENT_ERROR.type -> {
                if (!isAdded) {
                    return
                }
                setFragmentResult(
                    REQUEST_KEY_PAYMENT_STATUS, bundleOf(
                        KEY_STATUS to PaymentStatus.PAYMENT_ERROR
                    )
                )
                view?.findNavController()?.navigateUp()
            }
        }
        if (!paymentArguments.isNullOrEmpty())
            Utils.triggerFireBaseEvents(PAYMENT_STATUS, paymentArguments, activity)
    }

    override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
        showErrorScreen(ErrorHandlerActivity.COMMON_WITH_BACK_BUTTON)
    }

    private fun showErrorScreen(errorType: Int) {
        activity?.apply {
            val intent = Intent(this, ErrorHandlerActivity::class.java)
            intent.putExtra(ErrorHandlerActivity.ERROR_TYPE, errorType)
            startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentSuccessURI.isNotEmpty() && isAdded) {
            onStatusChanged(currentSuccessURI)
            currentSuccessURI = getString(R.string.empty)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        initPaymentWebView()
                    }
                }
            }
        }
    }

    override fun onDownloadRequested(
        url: String?,
        suggestedFilename: String?,
        mimeType: String?,
        contentLength: Long,
        contentDisposition: String?,
        userAgent: String?,
    ) {
    }

    override fun onExternalPageRequest(url: String?) {
    }
}