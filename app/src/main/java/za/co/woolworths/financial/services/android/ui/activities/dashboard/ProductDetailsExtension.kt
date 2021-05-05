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
        const val HTTP_OK: Int = 200
        const val PRODUCT_NOT_FOUND: Int = 502
        const val OUT_OF_STOCK_RESPONSE_CODE: String = "0813"

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
                                HTTP_OK -> activity?.apply {
                                    val bundle = Bundle()
                                    bundle.putString("strProductList", Gson().toJson(response.product))
                                    bundle.putString("strProductCategory", response.product?.productName)
                                    bundle.putString("productResponse", Gson().toJson(response))
                                    bundle.putBoolean("fetchFromJson", true)
                                    listner.onSuccess(bundle)
                                }
                                PRODUCT_NOT_FOUND -> activity.apply {
                                    if (response?.response?.code.equals(OUT_OF_STOCK_RESPONSE_CODE)) {
                                        listner.onProductNotFound(getString(R.string.out_of_stock_502))
                                    } else
                                        listner.onProductNotFound(getString(R.string.unable_to_process_502))
                                }
                                else -> {
                                    if (!WoolworthsApplication.isApplicationInForeground())
                                        return
                                    listner.onFailure()
                                    finish()
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
        fun onSuccess(bundle: Bundle)
        fun onFailure()
        fun onProductNotFound(message: String)
        fun startProgressBar()
        fun stopProgressBar()
    }
}