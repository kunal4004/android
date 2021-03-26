package za.co.woolworths.financial.services.android.ui.activities.dashboard

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

/**
 * Created by Kunal Uttarwar on 24/3/21.
 */
class ProductDetailsExtension : Fragment() {
    companion object {
        const val TAG: String = "BottomNavigationActivity"

        @JvmStatic
        fun retrieveProduct(productId: String, skuId: String, activity: Activity, listner: ProductDetailsStatusListner) {
            mGetProductDetail = OneAppService.productDetail(productId, skuId).apply {
                enqueue(CompletionHandler(object : IResponseListener<ProductDetailResponse> {
                    override fun onSuccess(response: ProductDetailResponse?) {
                        if (!WoolworthsApplication.isApplicationInForeground())
                            return
                        listner.stopProgressBar()
                        activity?.apply {
                            when (response?.httpCode) {
                                200 -> activity?.apply {
                                    val bundle = Bundle()
                                    bundle.putString("strProductList", Gson().toJson(response.product))
                                    bundle.putString("strProductCategory", response.product?.productName)
                                    bundle.putString("productResponse", Gson().toJson(response))
                                    bundle.putBoolean("fetchFromJson", true)
                                    listner.onSuccess(bundle)
                                }
                                else -> {
                                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, Utils.getString(this, R.string.statement_send_email_false_desc))
                                    val arguments = HashMap<String, String>()
                                    arguments[skuId] = "NO PRICE INFO"
                                    arguments[skuId] = "From BottomNavigation Promotions"
                                    Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        if (!WoolworthsApplication.isApplicationInForeground())
                            return
                        listner.onFailure()
                        activity?.apply { runOnUiThread { ErrorHandlerView(this).showToast() } }
                    }

                }, ProductDetailResponse::class.java))
            }
        }

        var mGetProductDetail: Call<ProductDetailResponse>? = null
    }

    interface ProductDetailsStatusListner {
        fun onSuccess(bundle:Bundle)
        fun onFailure()
        fun startProgressBar()
        fun stopProgressBar()
    }
}