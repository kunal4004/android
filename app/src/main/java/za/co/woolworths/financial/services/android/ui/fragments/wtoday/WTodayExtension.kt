package za.co.woolworths.financial.services.android.ui.fragments.wtoday

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WtodayMainFragmentBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

abstract class WTodayExtension : Fragment(R.layout.wtoday_main_fragment) {

    companion object {
        const val SCROLL_UP_ANIM_DURATION: Long = 300
        const val TAG: String = "WTodayFragment"
        var wTodayUrl: String = AppConfigSingleton.wwTodayURI ?: ""
    }

    abstract fun progressBarVisibility(isDisplayed: Boolean)

    protected lateinit var binding: WtodayMainFragmentBinding
    var mGetProductDetail: Call<ProductDetailResponse>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = WtodayMainFragmentBinding.bind(view)
    }

    fun retrieveProduct(productId: String, skuId: String) {
        progressBarVisibility(true)
        mGetProductDetail =  OneAppService().productDetail(productId, skuId).apply {
            enqueue(CompletionHandler(object : IResponseListener<ProductDetailResponse> {
                override fun onSuccess(response: ProductDetailResponse?) {
                    if (!WoolworthsApplication.isApplicationInForeground() && !isAdded && !isVisible && !userVisibleHint && !isHidden)
                        return
                    progressBarVisibility(false)
                    activity?.apply {
                        when (response?.httpCode) {
                            200 -> activity?.apply {
                                val bundle = Bundle()
                                bundle.putString("strProductList", Gson().toJson(response.product))
                                bundle.putString("strProductCategory", response.product?.productName)
                                bundle.putString("productResponse", Gson().toJson(response))
                                bundle.putBoolean("fetchFromJson", true)
                                ScreenManager.presentProductDetails(supportFragmentManager, R.id.webWToday, bundle)
                            }
                            else -> {
                                Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, Utils.getString(this, R.string.statement_send_email_false_desc))
                                val arguments = HashMap<String, String>()
                                arguments[skuId] = "NO PRICE INFO"
                                arguments[skuId] = "From WTodayFragment Promotions"
                                Utils.triggerFireBaseEvents(FirebaseAnalytics.Event.VIEW_ITEM, arguments, this)
                            }
                        }
                    }
                }

                override fun onFailure(error: Throwable?) {
                    if (!WoolworthsApplication.isApplicationInForeground() && !isAdded && !isVisible && !userVisibleHint && !isHidden)
                        return
                    progressBarVisibility(false)
                        activity?.apply { runOnUiThread { ErrorHandlerView(this).showToast() } }
                }

            },ProductDetailResponse::class.java))
        }
    }
}