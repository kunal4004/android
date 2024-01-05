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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCheckoutPaymentWebBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.checkout.service.network.PaymentAnalyticsData
import za.co.woolworths.financial.services.android.checkout.service.network.ShippingDetailsResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.PAYMENT_STATUS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.STATUS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.STATUS_URL
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.TRANSACTION_ID
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyValues.Companion.CURRENCY_VALUE
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyChooseVariationCallViewModel
import za.co.woolworths.financial.services.android.util.AdvancedWebView
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_ENDLESS_AISLE_JOURNEY
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.*
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import java.net.URI


@AndroidEntryPoint
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
        PAY_IN_STORE("payinstore"),
        PAYMENT_SUCCESS("success"),
        PAYMENT_ABANDON("abandon"),
        PAYMENT_UNAUTHENTICATED("unauthenticated"),
        PAYMENT_ERROR("error")
    }

    private lateinit var binding: FragmentCheckoutPaymentWebBinding
    private var currentSuccessURI = ""
    private var cartItemList: ArrayList<CommerceItem>? = null
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null
    private var isEndlessAisleJourney: Boolean? = false
    private val dyChooseVariationViewModel: DyChooseVariationCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? CheckoutActivity)?.apply {
            supportActionBar?.hide()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCheckoutPaymentWebBinding.bind(view)
        cartItemList = arguments?.getSerializable(CheckoutAddressManagementBaseFragment.CART_ITEM_LIST) as ArrayList<CommerceItem>?
        isEndlessAisleJourney = arguments?.getBoolean(IS_ENDLESS_AISLE_JOURNEY)
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

            var paymentUrl = when(isEndlessAisleJourney) {
                true -> AppConfigSingleton.nativeCheckout?.checkoutPaymentUrlPayInStore
                false -> AppConfigSingleton.nativeCheckout?.checkoutPaymentURL
                else -> AppConfigSingleton.nativeCheckout?.checkoutPaymentURL
            }

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

    private fun navigateToOrderConfirmation(isEndlessAisle: Boolean = false) {
        binding.paymentSuccessConfirmationLayout?.root?.visibility = View.VISIBLE
        if (isEndlessAisle) {
            binding.paymentSuccessConfirmationLayout.txtOrderPaymentConfirmed.text = getString(R.string.barcode_generated)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            binding.paymentSuccessConfirmationLayout?.root?.visibility = View.GONE
            val bundle = Bundle()
            bundle.putBoolean(IS_ENDLESS_AISLE_JOURNEY, isEndlessAisle)
            view?.let {
                GeoUtils.navigateSafe(it, R.id.action_checkoutPaymentWebFragment_orderConfirmationFragment, bundle)
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
        if (!paymentStatusType.isNullOrEmpty()) {
            paymentArguments[STATUS] = paymentStatusType
            paymentArguments[STATUS_URL] = currentSuccessURI
        }

        if (!transactionAnalytics.isNullOrEmpty()) {
            val jsonToAnalyticsList = Gson().fromJson<PaymentAnalyticsData?>(
                transactionAnalytics,
                object : TypeToken<PaymentAnalyticsData?>() {}.type
            )
            if (jsonToAnalyticsList != null)
                paymentArguments[TRANSACTION_ID] = jsonToAnalyticsList?.transaction_id ?: ""
            paymentArguments[PAYMENT_VALUE] = jsonToAnalyticsList?.value?.toString() ?: "0.0"
            paymentArguments[PAYMENT_TYPE] = jsonToAnalyticsList?.payment_type ?: ""
            AppConfigSingleton.dynamicYieldConfig?.apply {
                if (isDynamicYieldEnabled == true)
                    preparePaymentPageViewRequest(jsonToAnalyticsList)
            }
        }

        when (paymentStatusType) {
            PaymentStatus.PAYMENT_SUCCESS.type -> {
                val eventParams = Bundle()
                eventParams.apply {

                    cartItemList?.let {
                        val itemsArray = arrayListOf<Bundle>()
                        for (cartItem in it) {
                            val selectItems = Bundle()
                            selectItems.apply {
                                putString(
                                    FirebaseAnalytics.Param.ITEM_ID,
                                    cartItem.commerceItemInfo.productId
                                )

                                putString(
                                    FirebaseAnalytics.Param.ITEM_NAME,
                                    cartItem.commerceItemInfo.productDisplayName
                                )

                                putDouble(
                                    FirebaseAnalytics.Param.PRICE,
                                    cartItem.priceInfo.amount
                                )

                                putString(
                                    FirebaseAnalytics.Param.ITEM_BRAND,
                                    cartItem.commerceItemInfo?.productDisplayName
                                )
                                putString(
                                    FirebaseAnalytics.Param.ITEM_VARIANT,
                                    cartItem.commerceItemInfo?.size
                                )

                                putString(
                                    FirebaseAnalytics.Param.ITEM_CATEGORY,
                                    cartItem.commerceItemInfo.productDisplayName
                                )
                                putInt(
                                    FirebaseAnalytics.Param.QUANTITY,
                                    cartItem.commerceItemInfo.quantity
                                )
                                itemsArray.add(this)

                            }

                        }

                        putParcelableArray(
                            FirebaseAnalytics.Param.ITEMS,
                            itemsArray.toTypedArray()
                        )
                    }
                    putString(
                        FirebaseAnalytics.Param.CURRENCY,
                        CURRENCY_VALUE
                    )
                    putString(
                        FirebaseAnalytics.Param.PAYMENT_TYPE,
                        paymentArguments[PAYMENT_TYPE]
                    )
                    putDouble(
                        FirebaseAnalytics.Param.VALUE,
                        paymentArguments[PAYMENT_VALUE]?.toDouble() ?: 0.0
                    )

                    AnalyticsManager.logEvent(
                        FirebaseManagerAnalyticsProperties.ADD_PAYMENT_INFO,
                        this
                    )
                }
                navigateToOrderConfirmation()
            }
            PaymentStatus.PAY_IN_STORE.type ->{
                // clearing the paymentArguments because need to avoid analytics for endless order
                // and below we have check if its empty then don't call analytics
                paymentArguments.clear()
                navigateToOrderConfirmation(true)
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

    private fun preparePaymentPageViewRequest(jsonToAnalyticsList: PaymentAnalyticsData?) {
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress, config?.getDeviceModel())
        val dataOther = DataOther(null,null,ZAR,jsonToAnalyticsList?.payment_type,jsonToAnalyticsList?.value,null)
        val dataOtherArray: ArrayList<DataOther>? = ArrayList<DataOther>()
        dataOtherArray?.add(dataOther)
        val page = Page(null, PAYMENT_PAGE, OTHER, null, dataOtherArray)
        val context = Context(device, page, Utils.DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyChooseVariationViewModel.createDyRequest(homePageRequestEvent)
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