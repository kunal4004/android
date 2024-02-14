package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class NotifyMe (
		@SerializedName("email") val email: String,
		@SerializedName("phone_number") val phoneNumber: String,
		@SerializedName("first_name") val firstName: String,
		@SerializedName("product_id") val productId: String,
		@SerializedName("sku_id") val skuId: String,
		@SerializedName("store_id") val storeId: String,
		@SerializedName("place_id") val placeId: String,
		@SerializedName("status") val status: String,
		@SerializedName("subscribed_date") val subscribedDate: String,
		@SerializedName("notification_pending_count") val notificationPendingCount: String,
		@SerializedName("source_system") val sourceSystem: String,
		@SerializedName("frequency_hours") val frequencyHours: String,
) : Parcelable, Serializable