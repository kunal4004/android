package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.DiscountDetails
import za.co.woolworths.financial.services.android.models.network.Status

/**
 * Created by W7099877 on 2018/02/08.
 */
@Parcelize
data class OrderSummary(
    var totalItemsCount: Int = 0,
    var total: Double = 0.0,
    var estimatedDelivery: Double = 0.0,
    var basketTotal: Double = 0.0,
    var shippingAdjusted: Boolean = false,
    var savedAmount: Double = 0.0,
    var staffDiscount: Double,
    var tip: Double = 0.0,
    var suburb: @RawValue Suburb? = null,
    var state: String? = null,
    var submittedDate: String?,
    var completedDate: String?,
    var deliveryDates: @RawValue JsonElement? = null,
    @JvmField
    var discountDetails: DiscountDetails? = null,
    var store: Store? = null,
    var fulfillmentDetails: FulfillmentDetails? = null,
    var orderId: String? = "",
    var isChatEnabled: Boolean,
    var isDriverTrackingEnabled: Boolean,
    var shopperName: String?,
    var shopperId: String?,
    var orderStatus: @RawValue Any,
    var taxNoteNumbers: ArrayList<String>?,
    var requestCancellation: Boolean = false,
    var clickAndCollectOrder: Boolean = false,
    var orderCancellable: Boolean = false,
    var groupSubTotal: String = "",
    var giftCardAuthorizedAmount: Double = 0.0,
    var wrewardsDiscount: Double = 0.0,
    var storeDetails: StoreDetails? = null,
    var deliveryStatus: @RawValue Any? = null,
    var totalOrderCount: Double = 0.0,
    var driverTrackingURL : String? = "",
    var hasMinimumBasketAmount: Boolean = false,
    var minimumBasketAmount: Double = 0.0,
    var cashVoucherApplied: Double = 0.0,
    var endlessAisleOrder:Boolean = true,
    var endlessAisleBarcode:String = "28004100000070154000"
) : Parcelable