package za.co.woolworths.financial.services.android.ui.fragments.wtoday

import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebViewClient.ERROR_CONNECT
import android.webkit.WebViewClient.ERROR_TIMEOUT
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.add_to_list_content.*
import kotlinx.android.synthetic.main.wtoday_main_fragment.*
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IWTodayInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetails
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.isConnectedToNetwork
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.GridFragment
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

class WToday : Fragment(), IWTodayInterface {

    companion object {
        const val SCROLL_UP_ANIM_DURATION: Long = 300
        const val TAG: String = "WToday"
        var wTodayUrl: String? = WoolworthsApplication.getWwTodayURI() ?: ""
    }

    private var mGetProductDetail: HttpAsyncTask<String, String, ProductDetailResponse>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it) }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.wtoday_main_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
        setClient()
    }

    private fun configureUI() {
        webWToday?.apply {
            settings?.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                addJavascriptInterface(WebViewJavascriptInterface(this@WToday), "Android")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            loadUrl(wTodayUrl)
        }

        btnRetry?.setOnClickListener {
            if (isConnectedToNetwork()!!) {
                webWToday?.reload()
                noConnectionLayoutVisibility(GONE)
            } else {
                noConnectionLayoutVisibility(VISIBLE)
            }
        }
    }

    private fun setClient() {
        webWToday?.webViewClient = object : WebViewClient() {
            @TargetApi(Build.VERSION_CODES.M)
            override fun onReceivedError(webView: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(webView, request, error)
                handleError(error.errorCode, error.description.toString())
            }

            @SuppressWarnings("deprecation")
            override fun onReceivedError(webView: WebView, errorCode: Int, description: String, url: String) {
                super.onReceivedError(webView, errorCode, description, url)
                handleError(errorCode, description)
            }
        }
    }

    fun scrollToTop() {
        webWToday?.apply {
            ObjectAnimator.ofInt(this, "scrollY", scrollY, 0)?.run {
                setDuration(SCROLL_UP_ANIM_DURATION).start()
            }
        }
    }

    private fun handleError(errorCode: Int, description: String) {
        when (errorCode) {
            ERROR_CONNECT, ERROR_TIMEOUT -> {
                noConnectionLayoutVisibility(VISIBLE)
            }
            else -> {
                Log.d(TAG, "errCode: $errorCode desc : $description")
            }
        }
    }

    private fun noConnectionLayoutVisibility(state: Int) {
        incNoConnectionHandler?.visibility = state
    }

    override fun onShowProductListing(categoryId: String, categoryName: String) {
        val gridFragment = GridFragment().withArgs {
            putString("sub_category_id", categoryId)
            putString("sub_category_name", categoryName)
            putString("str_search_product", "")
        }
        (activity as? BottomNavigationActivity)?.pushFragment(gridFragment)
    }

    override fun onAddIngredientsToShoppingList(ingredients: String) {
        Log.d("onAddIngredientsToShop", ingredients)
    }

    override fun onShowProductDetail(productId: String, skuId: String) {
        activity?.runOnUiThread {
            showProductDetailProgressBar(VISIBLE)
            mGetProductDetail = GetProductDetails(productId, skuId, object : AsyncAPIResponse.ResponseDelegate<ProductDetailResponse> {
                override fun onSuccess(response: ProductDetailResponse) {
                    showProductDetailProgressBar(GONE)
                    if (isAdded && isVisible && userVisibleHint && !isHidden) {
                        activity?.apply {
                            when (response.httpCode) {
                                200 -> navigateToProductDetail(response, this@apply)
                                else -> {
                                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, Utils.getString(this, R.string.statement_send_email_false_desc))
                                    val arguments = HashMap<String, String>()
                                    arguments[skuId] = "NO PRICE INFO"
                                    arguments[skuId] = "From WToday Promotions"
                                    Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(errorMessage: String) {
                    showProductDetailProgressBar(GONE)
                    if (isAdded && isVisible && userVisibleHint && !isHidden) {
                        activity?.apply {
                            ErrorHandlerView(this).showToast()
                        }
                    }
                }

            }).execute() as? HttpAsyncTask<String, String, ProductDetailResponse>?
        }
    }

    private fun showProductDetailProgressBar(state: Int) {
        flProgressContainer?.visibility = state
    }

    private fun navigateToProductDetail(response: ProductDetailResponse, activity: FragmentActivity) {
        val bundle = Bundle()
        bundle.putString("strProductList", Gson().toJson(response.product))
        bundle.putString("strProductCategory", response.product?.productName)
        bundle.putString("productResponse", Gson().toJson(response))
        bundle.putBoolean("fetchFromJson", true)
        ScreenManager.presentProductDetails(activity, bundle)
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
        if (isCancelled) {
            cancel(true)
        }
    }
}