package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotifyMeRequest(
		@SerializedName("email") val email: String,
		@SerializedName("product_id") val productId: String?,
		@SerializedName("sku_id") val skuId: String?,
		@SerializedName("store_id") val storeId: String?,
		@SerializedName("place_id") val placeId: String?,
		@SerializedName("notification_pending_count") val notificationPendingCount: Int?,
		@SerializedName("frequency_hours") val frequencyHours: Int?,
		@SerializedName("source_system") val sourceSystem: String?
) : Parcelable
