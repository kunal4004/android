package za.co.woolworths.financial.services.android.ui.activities.deep_link

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

open class RetrieveProductDetail(private val activity: Activity?, private val productId: String, private val skuId: String, private val presentScreenIsFirstTime: Boolean) {
    fun retrieveProduct() {
        OneAppService.productDetail(productId, skuId).enqueue(CompletionHandler(object : IResponseListener<ProductDetailResponse> {
            override fun onSuccess(response: ProductDetailResponse?) {
                if (WoolworthsApplication.isApplicationInForeground() && activity != null) {
                    when (response?.httpCode) {
                        200 -> {

                            if (presentScreenIsFirstTime) {
                                ScreenManager.presentOnboarding(activity)
                            } else {
                                val openBottomActivity = Intent(activity, BottomNavigationActivity::class.java)
                                activity.startActivity(openBottomActivity)
                                activity.overridePendingTransition(0, 0)
                            }
                            val bundle = Bundle()
                            bundle.putString("strProductList", Gson().toJson(response.product))
                            bundle.putString("strProductCategory", response.product?.productName)
                            bundle.putString("productResponse", Gson().toJson(response))
                            bundle.putBoolean("fetchFromJson", true)
                            ScreenManager.presentProductDetails(activity, bundle)
                            activity.finish()
                        }
                        else -> {
                            Utils.displayValidationMessage(WoolworthsApplication.getAppContext(), CustomPopUpWindow.MODAL_LAYOUT.ERROR, Utils.getString(WoolworthsApplication.getAppContext(), R.string.statement_send_email_false_desc))
                            val arguments = HashMap<String, String>()
                            skuId.run {
                                arguments[this] = "NO PRICE INFO"
                                arguments[this] = "From WTodayFragment Promotions"
                            }
                            Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments, activity)
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                if (WoolworthsApplication.isApplicationInForeground()) {
                    activity?.apply { runOnUiThread { ErrorHandlerView(this).showToast() } }
                }
            }

        }, ProductDetailResponse::class.java))
    }
}