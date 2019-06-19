package za.co.woolworths.financial.services.android.ui.fragments.wtoday

import android.os.Bundle
import android.support.v4.app.Fragment
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetails
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

abstract class WTodayExtension : Fragment() {

    companion object {
        const val SCROLL_UP_ANIM_DURATION: Long = 300
        const val TAG: String = "WTodayFragment"
        var wTodayUrl: String? = WoolworthsApplication.getWwTodayURI() ?: ""
    }

    abstract fun progressBarVisibility(isDisplayed: Boolean)

    var mGetProductDetail: HttpAsyncTask<String, String, ProductDetailResponse>? = null

    fun retrieveProduct(productId: String, skuId: String) {
        progressBarVisibility(true)
        mGetProductDetail = GetProductDetails(productId, skuId, object : AsyncAPIResponse.ResponseDelegate<ProductDetailResponse> {
            override fun onSuccess(response: ProductDetailResponse) {
                progressBarVisibility(false)
                activity?.apply {
                    when (response.httpCode) {
                        200 -> activity?.apply {
                            val bundle = Bundle()
                            bundle.putString("strProductList", Gson().toJson(response.product))
                            bundle.putString("strProductCategory", response.product?.productName)
                            bundle.putString("productResponse", Gson().toJson(response))
                            bundle.putBoolean("fetchFromJson", true)
                            ScreenManager.presentProductDetails(activity, bundle)
                        }
                        else -> {
                            Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, Utils.getString(this, R.string.statement_send_email_false_desc))
                            val arguments = HashMap<String, String>()
                            arguments[skuId] = "NO PRICE INFO"
                            arguments[skuId] = "From WTodayFragment Promotions"
                            Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments)
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                progressBarVisibility(false)
                if (WoolworthsApplication.isApplicationInForeground() && isAdded && isVisible && userVisibleHint && !isHidden) {
                    activity?.apply { runOnUiThread { ErrorHandlerView(this).showToast() } }
                }
            }

        }).execute() as? HttpAsyncTask<String, String, ProductDetailResponse>?
    }
}