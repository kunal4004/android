package za.co.woolworths.financial.services.android.util

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class PostItemToCart {

    fun make(addItemToCart: MutableList<AddItemToCart>, requestBuilder: RequestListener<AddItemToCartResponse>): Call<AddItemToCartResponse> {
        val postItemRequest = OneAppService.addItemToCart(addItemToCart)
        postItemRequest.enqueue(CompletionHandler(object : RequestListener<AddItemToCartResponse> {
            override fun onSuccess(addItemToCartResponse: AddItemToCartResponse) {

                // Ensure counter is always updated after a successful add to cart
                when (addItemToCartResponse.httpCode) {
                    200 -> QueryBadgeCounter.getInstance().queryCartSummaryCount()
                    else -> {
                    }
                }
                requestBuilder.onSuccess(addItemToCartResponse)
            }

            override fun onFailure(error: Throwable?) {
                requestBuilder.onFailure(error)
            }

        }, AddItemToCartResponse::class.java))

        return postItemRequest
    }

}
