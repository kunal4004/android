package za.co.woolworths.financial.services.android.util

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.CartSummaryResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK_201

class GetCartSummary {

    fun getCartSummary(response: IResponseListener<CartSummaryResponse>): Call<CartSummaryResponse> {
        val cartSummaryRequest = OneAppService().getCartSummary()
        cartSummaryRequest.enqueue(CompletionHandler(object :
            IResponseListener<CartSummaryResponse> {
            override fun onSuccess(cartSummaryResponse: CartSummaryResponse?) {
                when (cartSummaryResponse?.httpCode) {

                    HTTP_OK, HTTP_OK_201 -> {
                        cartSummaryResponse.apply {
                            cacheSuburbFromCartSummary(this)
                            cacheDeliveryDetails(this)
                            response.onSuccess(this)
                        }
                    }
                    else -> {
                        Utils.savePreferredDeliveryLocation(null)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                response.onFailure(error)
            }
        }, CartSummaryResponse::class.java))
        return cartSummaryRequest
    }

    private fun cacheDeliveryDetails(cartSummaryResponse: CartSummaryResponse) {
        cartSummaryResponse?.data?.getOrNull(0)?.deliveryDetails?.apply {
                Utils.saveDeliveryDetails(this)
            }
        }


    private fun cacheSuburbFromCartSummary(cartSummaryResponse: CartSummaryResponse?) {
        cartSummaryResponse?.data?.get(0)?.fulfillmentDetails?.apply {
            this.deliveryType?.let {
                Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(this))
            }
        }
    }
}