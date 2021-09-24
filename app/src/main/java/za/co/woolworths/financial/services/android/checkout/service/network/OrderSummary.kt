package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OrderSummary : Serializable {
    @SerializedName("totalItemsCount")
    var totalItemsCount: Long? = null

    @SerializedName("groupSubTotal")
    var groupSubTotal: String? = null

    @SerializedName("giftCardAuthorizedAmount")
    var giftCardAuthorizedAmount: Long? = null

    @SerializedName("orderId")
    var orderId: String? = null

    @SerializedName("basketTotal")
    var basketTotal: Double? = null

    @SerializedName("shippingAdjusted")
    var shippingAdjusted: Boolean? = null

    @SerializedName("wrewardsDiscount")
    var wrewardsDiscount: Long? = null

    @SerializedName("store")
    var store: Store? = null

    @SerializedName("submittedDate")
    var submittedDate: String? = null

    @SerializedName("completedDate")
    var completedDate: String? = null

    @SerializedName("orderCancellable")
    var orderCancellable: Boolean? = null

    @SerializedName("total")
    var total: Double? = null

    @SerializedName("discountDetails")
    var discountDetails: DiscountDetails? = null

    @SerializedName("suburb")
    var suburb: Suburb? = null

    @SerializedName("links")
    var links: List<Any>? = null

    @SerializedName("state")
    var state: String? = null

    @SerializedName("estimatedDelivery")
    var estimatedDelivery: Long? = null

    @SerializedName("deliveryStatus")
    var deliveryStatus: String? = null

    @SerializedName("totalOrderCount")
    var totalOrderCount: Long? = null

    @SerializedName("savedAmount")
    var savedAmount: Long? = null

    @SerializedName("deliveryDates")
    var deliveryDates: String? = null
}