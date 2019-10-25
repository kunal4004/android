package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class ProductDetailsInteractorImpl() : ProductDetailsContract.ProductDetailsInteractor {

    override fun getProductDetails(productRequest: ProductRequest, onFinishListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        request(OneAppService.productDetail(productRequest.productId, productRequest.skuId), onFinishListener)
    }

    override fun getCartSummary() {
    }

    override fun getStockAvailability(storeID: String, multiSKU: String, onFinishListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        request(OneAppService.getInventorySkuForStore(storeID, multiSKU), onFinishListener)
    }

    override fun postAddItemToCart() {
    }

    override fun getLocationItems() {
    }

    private inline fun <reified RESPONSE_OBJECT> request(call: Call<RESPONSE_OBJECT>, requestListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        val classType: Class<RESPONSE_OBJECT> = RESPONSE_OBJECT::class.java
        call.enqueue(CompletionHandler(object : RequestListener<RESPONSE_OBJECT> {
            override fun onSuccess(response: RESPONSE_OBJECT) {
                requestListener?.onSuccess(response)
            }

            override fun onFailure(error: Throwable?) {
                requestListener?.onFailure(error)
            }
        }, classType))
    }
}