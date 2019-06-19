package za.co.woolworths.financial.services.android.ui.activities.deep_link

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.AsyncAPIResponse
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetails
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

open class RetrieveProductDetail(private val activity: Activity?, private val productId: String?, private val skuId: String?, private val presentScreenIsFirstTime: Boolean) {
    fun retrieveProduct() {
        GetProductDetails(productId, skuId, object : AsyncAPIResponse.ResponseDelegate<ProductDetailResponse> {
            override fun onSuccess(response: ProductDetailResponse) {
                if (WoolworthsApplication.isApplicationInForeground() && activity != null) {
                    when (response.httpCode) {
                        200 -> {

                            if (presentScreenIsFirstTime) {
                                ScreenManager.presentOnboarding(activity)
                            } else {
                                val openBottomActivity = Intent(activity, BottomNavigationActivity::class.java)
                                openBottomActivity.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, "")
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
                            skuId?.apply {
                                arguments[this] = "NO PRICE INFO"
                                arguments[this] = "From WTodayFragment Promotions"
                            }
                            Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments)
                        }
                    }
                }
            }

            override fun onFailure(errorMessage: String) {
                if (WoolworthsApplication.isApplicationInForeground()) {
                    activity?.apply { runOnUiThread { ErrorHandlerView(this).showToast() } }
                }
            }

        }).execute()
    }
}