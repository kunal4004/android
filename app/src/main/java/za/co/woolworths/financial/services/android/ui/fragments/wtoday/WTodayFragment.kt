package za.co.woolworths.financial.services.android.ui.fragments.wtoday

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.*
import android.webkit.WebViewClient.ERROR_CONNECT
import android.webkit.WebViewClient.ERROR_TIMEOUT
import com.awfs.coordination.databinding.WtodayMainFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IWTodayInterface
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.isConnectedToNetwork
import za.co.woolworths.financial.services.android.ui.extension.isEmailValid
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment
import za.co.woolworths.financial.services.android.util.Utils

@Suppress("DEPRECATION")
class WTodayFragment : WTodayExtension(), IWTodayInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            Utils.updateStatusBarBackground(it)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configureUI()
        binding.setClient()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WtodayMainFragmentBinding.configureUI() {
        webWToday?.apply {
            settings?.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                addJavascriptInterface(WebViewJavascriptInterface(this@WTodayFragment), "Android")
                setSupportMultipleWindows(true)

                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            loadUrl(wTodayUrl)
        }

        incNoConnectionHandler.btnRetry?.setOnClickListener {
            if (isConnectedToNetwork()!!) {
                webWToday?.reload()
                noConnectionLayoutVisibility(GONE)
            } else {
                noConnectionLayoutVisibility(VISIBLE)
            }
        }

    }

    private fun WtodayMainFragmentBinding.setClient() {

        webWToday?.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(webView: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(webView, request, error)
                handleError(error.errorCode)
            }

            override fun onReceivedError(webView: WebView, errorCode: Int, description: String, url: String) {
                super.onReceivedError(webView, errorCode, description, url)
                handleError(errorCode)
            }
        }

        webWToday?.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                val data = view?.hitTestResult?.extra

                try {
                    var intent: Intent?
                    when (data?.isEmailValid()) {
                        true -> Utils.sendEmail(data)
                        else -> data?.let {
                            val uri = Uri.parse(it)
                            intent = Intent(Intent.ACTION_VIEW, uri)
                            activity?.packageManager?.let { packageManager -> if (intent?.resolveActivity(packageManager) != null) startActivity(intent) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message!!)
                }
                return false
            }
        }

    }

    fun scrollToTop() = binding.webWToday?.apply {
        ObjectAnimator.ofInt(this, "scrollY", scrollY, 0)?.run {
            setDuration(SCROLL_UP_ANIM_DURATION).start()
        }
    }

    private fun handleError(errorCode: Int) = when (errorCode) {
        ERROR_CONNECT, ERROR_TIMEOUT -> noConnectionLayoutVisibility(VISIBLE)
        else -> {
        }
    }


    private fun noConnectionLayoutVisibility(state: Int) {
        binding.incNoConnectionHandler?.root?.visibility = state
    }

    override fun onShowProductListing(categoryId: String, categoryName: String) {
        (activity as? BottomNavigationActivity)?.pushFragment(ProductListingFragment.newInstance(ProductsRequestParams.SearchType.NAVIGATE, categoryName, categoryId,
            isBrowsing = false,
            sendDeliveryDetails = false
        ))
    }

    override fun onAddIngredientsToShoppingList(ingredients: String) {
    }

    override fun onShowProductDetail(productId: String, skuId: String) {
        activity?.runOnUiThread { retrieveProduct(productId, skuId) }
    }

    override fun sendEventToFirebase(eventName: String, parameter: String) {
        val parameterName = eventName.replace("wtoday_", "")
        val arguments = HashMap<String, String>()
        arguments.put(parameterName, parameter)
        activity?.apply {Utils.triggerFireBaseEvents(eventName, arguments, this) }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.WTODAY) }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        when (hidden) {
            false -> (activity as? BottomNavigationActivity)?.hideToolbar()
            true -> cancelPDPRequest()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPDPRequest()
    }

    private fun cancelPDPRequest() = mGetProductDetail?.apply {
        if (isCanceled) {
            cancel()
        }
    }

    override fun progressBarVisibility(isDisplayed: Boolean) {
        binding.flProgressContainer?.visibility = if (isDisplayed) VISIBLE else GONE
    }
}