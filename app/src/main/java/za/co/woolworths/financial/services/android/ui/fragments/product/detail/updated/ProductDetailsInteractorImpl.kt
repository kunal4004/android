package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ProductRequest
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.GetCartSummary
import za.co.woolworths.financial.services.android.util.PostItemToCart

class ProductDetailsInteractorImpl() : ProductDetailsContract.ProductDetailsInteractor {

    override fun getProductDetails(productRequest: ProductRequest, onFinishListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        request(OneAppService.productDetail(productRequest.productId, productRequest.skuId), onFinishListener)
    }

    override fun getStockAvailability(storeID: String, multiSKU: String, onFinishListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        request(OneAppService.getInventorySkuForStore(storeID, multiSKU), onFinishListener)
    }

    override fun postAddItemToCart(addItemToCart: List<AddItemToCart>, onFinishListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
        PostItemToCart().make(addItemToCart as MutableList<AddItemToCart>, object : RequestListener<AddItemToCartResponse> {
            override fun onSuccess(cartSummaryResponse: AddItemToCartResponse?) {
                onFinishListener?.onSuccess(cartSummaryResponse)
            }

            override fun onFailure(error: Throwable) {
                onFinishListener?.onFailure(error)
            }
        })
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

    override fun getCartSummary(requestListener: ProductDetailsContract.ProductDetailsInteractor.OnFinishListener) {
         GetCartSummary().getCartSummary(object : RequestListener<CartSummaryResponse> {
            override fun onSuccess(cartSummaryResponse: CartSummaryResponse?) {
                requestListener?.onSuccess(cartSummaryResponse)
            }

            override fun onFailure(error: Throwable) {
                //getNavigator().onTokenFailure(error.toString())
                requestListener?.onFailure(error)
            }
        })
    }


}