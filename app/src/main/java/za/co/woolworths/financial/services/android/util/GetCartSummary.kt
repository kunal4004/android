package za.co.woolworths.financial.services.android.util

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.dto.*

class GetCartSummary {

    fun getCartSummary(response: RequestListener<CartSummaryResponse>): Call<CartSummaryResponse> {
        val cartSummaryRequest = OneAppService.getCartSummary()
        cartSummaryRequest.enqueue(CompletionHandler(object : RequestListener<CartSummaryResponse> {
            override fun onSuccess(cartSummaryResponse: CartSummaryResponse?) {
                cartSummaryResponse?.apply {
                    cacheSuburbFromCartSummary(this)
                    response.onSuccess(this)
                }
            }

            override fun onFailure(error: Throwable) {
                response.onFailure(error)
            }
        }, CartSummaryResponse::class.java))
        return cartSummaryRequest
    }

    private fun cacheSuburbFromCartSummary(cartSummaryResponse: CartSummaryResponse?) {
        cartSummaryResponse?.data?.get(0)?.apply {
            val province = Province().also { province ->
                province.name = provinceName
            }
            val suburb = Suburb().also { suburb ->
                suburb.id = suburbId
                suburb.name = suburbName
                suburb.fulfillmentStores = suburb.fulfillmentStores
            }
            Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(province, suburb))
        }
    }
}