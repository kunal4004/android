package za.co.woolworths.financial.services.android.util

import android.text.TextUtils
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.dto.*

class GetCartSummary {

    fun getCartSummary(response: IResponseListener<CartSummaryResponse>): Call<CartSummaryResponse> {
        val cartSummaryRequest = OneAppService.getCartSummary()
        cartSummaryRequest.enqueue(CompletionHandler(object : IResponseListener<CartSummaryResponse> {
            override fun onSuccess(cartSummaryResponse: CartSummaryResponse?) {
                cartSummaryResponse?.apply {
                    cacheSuburbFromCartSummary(this)
                    response.onSuccess(this)
                }
            }

            override fun onFailure(error: Throwable?) {
                response.onFailure(error)
            }
        }, CartSummaryResponse::class.java))
        return cartSummaryRequest
    }

    private fun cacheSuburbFromCartSummary(cartSummaryResponse: CartSummaryResponse?) {
        cartSummaryResponse?.data?.get(0)?.fulfillmentDetails?.apply {
            this.deliveryType?.let {
                Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(this))
            }
        }
    }
}