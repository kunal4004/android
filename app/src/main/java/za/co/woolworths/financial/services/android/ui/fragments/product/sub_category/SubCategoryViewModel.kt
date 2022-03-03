package za.co.woolworths.financial.services.android.ui.fragments.product.sub_category

import android.location.Location
import androidx.databinding.ObservableBoolean
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.SubCategories
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.OneAppService.getSubCategory
import za.co.woolworths.financial.services.android.ui.base.BaseViewModel
import za.co.woolworths.financial.services.android.util.Utils

class SubCategoryViewModel : BaseViewModel<SubCategoryNavigator>() {
    private var childItem = false
    private var subCategoryRequest: Call<SubCategories>? = null

    fun executeSubCategory(categoryId: String, version: String) {
        subCategoryRequest = getSubCategory(categoryId, version)
        subCategoryRequest?.enqueue(
            CompletionHandler(
                object : IResponseListener<SubCategories> {
                    override fun onSuccess(subCategories: SubCategories?) {
                        when (subCategories?.httpCode) {
                            200 -> {
                                navigator.bindSubCategoryResult(subCategories.subCategories)
                                navigator.onLoadComplete()
                            }
                            else -> {
                                val response = subCategories?.response
                                if (response != null) {
                                    navigator.unhandledResponseHandler(response)
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

    val loading: ObservableBoolean
        get() = isLoading

    fun cancelRequest() {
        if (subCategoryRequest != null && subCategoryRequest!!.isCanceled) {
            subCategoryRequest!!.cancel()
        }
    }

    fun setChildItem(childItem: Boolean) {
        this.childItem = childItem
    }

    fun childItem(): Boolean {
        return childItem
    }
}