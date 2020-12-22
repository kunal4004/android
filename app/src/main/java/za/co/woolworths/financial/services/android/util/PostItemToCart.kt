package za.co.woolworths.financial.services.android.util

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

class PostItemToCart {

    fun make(addItemToCart: MutableList<AddItemToCart>, requestBuilder: IResponseListener<AddItemToCartResponse>): Call<AddItemToCartResponse> {
        val postItemRequest = OneAppService.addItemToCart(addItemToCart)
        postItemRequest.enqueue(CompletionHandler(object : IResponseListener<AddItemToCartResponse> {
            override fun onSuccess(response: AddItemToCartResponse?) {

                // Ensure counter is always updated after a successful add to cart
                when (response?.httpCode) {
                    200 -> {
                        QueryBadgeCounter.instance.queryCartSummaryCount()
                        response.data[0]?.productCountMap?.quantityLimit?.foodMaximumQuantity?.let {
                            WoolworthsApplication.getClickAndCollect().maxNumberOfItemsAllowed = it
                        }
                    }
                    else -> {
                    }
                }
                requestBuilder.onSuccess(response)
            }

            override fun onFailure(error: Throwable?) {
                requestBuilder.onFailure(error)
            }

        }, AddItemToCartResponse::class.java))

        return postItemRequest
    }

}
