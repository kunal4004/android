package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category

import android.location.Location
import androidx.databinding.ObservableBoolean
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.SubCategories
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class SubCategoryViewModel : BaseViewModel<SubCategoryNavigator>() {
    private var childItem = false
    private var rootCategoryRequest: Call<RootCategories>? = null
    private var subCategoryRequest: Call<SubCategories>? = null

    fun fetchSubCategory(
        categoryId: String,
        version: String,
        isLocationEnabled: Boolean,
        location: Location?,
        retryCountOn502: Int = 3
    ) {
        subCategoryRequest = OneAppService().getSubCategory(categoryId, version)
        subCategoryRequest?.enqueue(
            CompletionHandler(
                object : IResponseListener<SubCategories> {
                    override fun onSuccess(subCategories: SubCategories?) {
                        when (subCategories?.httpCode) {
                            200 -> {
                                navigator.bindSubCategoryResult(subCategories.subCategories, version)
                                navigator.onLoadComplete()
                            }
                            502 -> {
                                if (retryCountOn502 > 0) {
                                    fetchRootCategory(
                                        categoryId,
                                        version,
                                        isLocationEnabled,
                                        location,
                                        retryCountOn502 - 1
                                    )
                                } else {
                                    subCategories?.response?.let {
                                        navigator.unhandledResponseHandler(it)
                                    }
                                }
                            }
                            else -> {
                                subCategories?.response?.let {
                                    navigator.unhandledResponseHandler(it)
                                }
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        navigator.onLoadComplete()
                        navigator.onFailureResponse(error.toString())
                    }
                }, SubCategories::class.java
            )
        )
    }

    fun fetchRootCategory(
        categoryId: String,
        version: String,
        isLocationEnabled: Boolean,
        location: Location?,
        retryCountOn502: Int = 3
    ) {
        rootCategoryRequest = OneAppService().getRootCategory(isLocationEnabled, location, getDeliveryType())
        rootCategoryRequest?.enqueue(CompletionHandler(object : IResponseListener<RootCategories> {
            override fun onSuccess(response: RootCategories?) {
                when (response?.httpCode) {
                    200 -> {
                        response.response?.version?.let { updatedVersion ->
                            fetchSubCategory(
                                categoryId,
                                updatedVersion,
                                isLocationEnabled,
                                location,
                                retryCountOn502
                            )
                        } ?: run {
                            fetchRootCategory(
                                categoryId,
                                version,
                                isLocationEnabled,
                                location,
                                retryCountOn502 - 1
                            )
                        }
                    }
                    502 -> {
                        if (retryCountOn502 > 0) {
                            fetchRootCategory(
                                categoryId,
                                version,
                                isLocationEnabled,
                                location,
                                retryCountOn502 - 1
                            )
                        } else {
                            response?.response?.let {
                                navigator.unhandledResponseHandler(it)
                            }
                        }
                    }
                    else -> {
                        response?.response?.let {
                            navigator.unhandledResponseHandler(it)
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                navigator.onLoadComplete()
                navigator.onFailureResponse(error.toString())
            }
        }, RootCategories::class.java))
    }

    val loading: ObservableBoolean
        get() = isLoading

    fun cancelRequest() {
        if (subCategoryRequest != null && subCategoryRequest!!.isCanceled) {
            subCategoryRequest!!.cancel()
        }
    }

    private fun getDeliveryType(): String {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let { fulfillmentDetails ->
                return Delivery.getType(fulfillmentDetails.deliveryType)?.name ?: BundleKeysConstants.STANDARD
            }
        } else {
            KotlinUtils.getAnonymousUserLocationDetails()?.fulfillmentDetails?.let { fulfillmentDetails ->
                return Delivery.getType(fulfillmentDetails.deliveryType)?.name ?: BundleKeysConstants.STANDARD
            }
        }
        return BundleKeysConstants.STANDARD
    }

    fun setChildItem(childItem: Boolean) {
        this.childItem = childItem
    }

    fun childItem(): Boolean {
        return childItem
    }
}