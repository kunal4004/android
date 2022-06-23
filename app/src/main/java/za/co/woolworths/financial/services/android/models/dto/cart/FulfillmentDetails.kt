package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import za.co.woolworths.financial.services.android.checkout.service.network.DeliveryStatus

@Parcelize
data class FulfillmentDetails(val foodMaximumQuantity: Int?, val address: Address?, val fulfillmentStores: @RawValue JsonElement?, val deliveryType: String?, val bulkPromotionEnabled: Boolean?, val deliverable: Boolean?, val storeName: String?, val storeId: String?, val otherMaximumQuantity: Int?, var liquorDeliverable:Boolean): Parcelable
