package za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.dummydata

import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.models.dto.cart.*
import za.co.woolworths.financial.services.android.utils.mock

object SubmittedOrdersResponseDummy {

    private fun orderSummary() = OrderSummary(
        completedDate = null,
        isChatEnabled = false,
        isDriverTrackingEnabled = false,
        orderStatus = mock(),
        shopperId = null,
        shopperName = null,
        staffDiscount = 0.0,
        submittedDate = null,
        taxNoteNumbers = null,
    )

    fun validSubmittedOrderResponse(): SubmittedOrderResponse {
        val foodItem1 = OrderItem().apply {
            productId = "food-product-1"
            quantity = 1
            priceInfo = PriceInfo().apply { amount = 15.0 }
        }

        val foodItem2 = OrderItem().apply {
            productId = "food-product-2"
            quantity = 2
            priceInfo = PriceInfo().apply { amount = 20.0 }
        }

        val foodItems = arrayOf(foodItem1, foodItem2)

        val otherItem1 = OrderItem().apply {
            productId = "other-product-1"
            quantity = 1
            priceInfo = PriceInfo().apply { amount = 25.0 }
        }

        val otherItem2 = OrderItem().apply {
            productId = "other-product-2"
            quantity = 2
            priceInfo = PriceInfo().apply { amount = 30.0 }
        }

        val otherItems = arrayOf(otherItem1, otherItem2)

        val orderItems = OrderItems()
        orderItems.food = foodItems
        orderItems.other = otherItems

        val submitOrderResponse = SubmittedOrderResponse()
        submitOrderResponse.orderSummary = orderSummary().apply { orderId = "oFakeOrderId" }
        submitOrderResponse.items = orderItems
        return submitOrderResponse
    }
}