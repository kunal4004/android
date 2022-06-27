package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails

/**
 * Created by W7099877 on 2018/02/08.
 */
@Parcelize
data class OrderSummary(
    var totalItemsCount: Int,
    var total: Double,
    var estimatedDelivery: Double,
    var basketTotal: Double,
    var shippingAdjusted: Boolean,
    var savedAmount: Double,
    var staffDiscount: Double,
    var tip: Double,
    var suburb: @RawValue Suburb? = null,
    var state: String?,
    var submittedDate: String?,
    var completedDate: String?,
    var deliveryDates: @RawValue JsonElement? = null,
    @JvmField
    var discountDetails: DiscountDetails? = null,
    var store: Store? = null,
    var fulfillmentDetails: FulfillmentDetails? = null,
    var orderId: String?,
    var isChatEnabled: Boolean,
    var isDriverTrackingEnabled: Boolean,
    var shopperName: String?,
    var orderStatus: String?,
    var taxNoteNumbers: ArrayList<String>?,
    var requestCancellation: Boolean = false,
    var clickAndCollectOrder: Boolean = false,
    var orderCancellable: Boolean = false
) : Parcelable