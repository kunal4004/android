package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase

import za.co.woolworths.financial.services.android.models.dto.cart.OrderItem
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CartProducts
import za.co.woolworths.financial.services.android.recommendations.data.response.request.CommonRecommendationEvent
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_PAGE_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_TYPE_PAGEVIEW
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants.EVENT_URL_ORDERDETAILS
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

class SubmitRecommendationsUseCase @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) {

    suspend operator fun invoke(submittedOrderResponse: SubmittedOrderResponse): Resource<RecommendationResponse> {
        val recommendationRequest = prepareSubmitRecommendationRequest(submittedOrderResponse)
        return recommendationsRepository.getRecommendationResponse(recommendationRequest, false, null)
    }

    private fun prepareSubmitRecommendationRequest(response: SubmittedOrderResponse): RecommendationRequest? {
        val monetateId = Utils.getMonetateId()
        val eventType = Constants.EVENT_TYPE_PURCHASE
        val orderId = response.orderSummary?.orderId
        val productLines = getPurchaseLines(response)

        return if (productLines.isNullOrEmpty() || monetateId.isNullOrEmpty() || orderId.isNullOrEmpty()) {
            null
        } else {
            RecommendationRequest(
                events = listOf(
                    Event(
                        eventType = EVENT_TYPE_PAGEVIEW,
                        pageType = EVENT_PAGE_TYPE,
                        url = EVENT_URL_ORDERDETAILS
                    ), Event(
                        eventType = eventType, orderId = orderId, purchaseLines = productLines
                    )
                ).plus(CommonRecommendationEvent.commonRecommendationEvents()),
                monetateId = monetateId
            )
        }
    }


    private fun getPurchaseLines(
        response: SubmittedOrderResponse
    ): List<CartProducts>? {
        val shippingCartProduct =
            getCartProductForShipping(response.deliveryDetails?.shippingAmount)
        val discountCartProduct =
            getCartProductForDiscount(response.orderSummary?.discountDetails?.totalOrderDiscount)

        val foodItems: List<CartProducts>? = orderedFoodItems(response.items?.food)
        val otherItems: List<CartProducts>? = otherOrderedItems(response.items?.other)

        return if (foodItems.isNullOrEmpty() && otherItems.isNullOrEmpty()) {
            null
        } else {
            val shippingAndDiscount = listOfNotNull(shippingCartProduct, discountCartProduct)
            val purchaseLines = mutableListOf<CartProducts>()
            foodItems?.let {
                purchaseLines.addAll(it)
            }

            otherItems?.let {
                purchaseLines.addAll(it)
            }

            purchaseLines.addAll(shippingAndDiscount)
            purchaseLines
        }
    }

    private fun otherOrderedItems(otherItems: Array<OrderItem>?) =

        otherItems?.map { items ->
            CartProducts(
                pid = items.productId,
                sku = items.productId,
                quantity = items.quantity,
                value = getAmountOnlyLastTwoDecimal(items.priceInfo?.amount),
                currency = Constants.CURRENCY_VALUE
            )
        }

    fun getAmountOnlyLastTwoDecimal(price: Double?): String? {
        return try {
            "%.2f".format(price ?: 0.00)
        } catch (e: Exception) {
            "0.00"
        }
    }

    private fun orderedFoodItems(foodItems: Array<OrderItem>?) =
        foodItems?.map { foodItem ->
            CartProducts(
                pid = foodItem.productId,
                sku = foodItem.productId,
                quantity = foodItem.quantity,
                value = getAmountOnlyLastTwoDecimal(foodItem.priceInfo?.amount),
                currency = Constants.CURRENCY_VALUE
            )
        }

    private fun getCartProductForShipping(shippingAmount: Double?): CartProducts? {
        return if ((shippingAmount ?: 0.0) > 0) {
            CartProducts(
                pid = Constants.PRODUCT_ID_FOR_SHIPPING,
                sku = Constants.PRODUCT_ID_FOR_SHIPPING,
                quantity = Constants.QUANTITY_FOR_SHIPPING,
                value =  getAmountOnlyLastTwoDecimal(shippingAmount),
                currency = Constants.CURRENCY_VALUE
            )
        } else {
            null
        }
    }

    private fun getCartProductForDiscount(totalOrderDiscount: Double?): CartProducts? {
        return if ((totalOrderDiscount ?: 0.0) > 0) {
            CartProducts(
                pid = Constants.PRODUCT_ID_FOR_DISCOUNT,
                sku = Constants.PRODUCT_ID_FOR_DISCOUNT,
                quantity = Constants.QUANTITY_FOR_DISCOUNT,
                value =  getAmountOnlyLastTwoDecimal(totalOrderDiscount),
                currency = Constants.CURRENCY_VALUE
            )
        } else {
            null
        }
    }
}