package za.co.woolworths.financial.services.android.ui.extension

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefineProductsResult

fun refineProducts(onRefineProductsResult: OnRefineProductsResult, productsRequestParams: ProductsRequestParams): Call<ProductView> {
    val resultListener: OnRefineProductsResult? = onRefineProductsResult
    productsRequestParams.responseType = ProductsRequestParams.ResponseType.SUMMARY
    val productRequest = OneAppService.getProducts(productsRequestParams)
    productRequest.enqueue(CompletionHandler(object : IResponseListener<ProductView> {
        override fun onSuccess(productView: ProductView?) {
            productView?.apply {
                if (httpCode == 200) {
                    resultListener?.onProductRefineSuccess(this, productsRequestParams.refinement)
                } else {
                    response?.desc?.let {  desc -> resultListener?.onProductRefineFailure(desc)}
                }
            }
        }

        override fun onFailure(error: Throwable?) {
            error?.message?.let { message -> resultListener?.onProductRefineFailure(message)}
        }

    },ProductView::class.java))
   return productRequest

}