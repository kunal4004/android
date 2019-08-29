package za.co.woolworths.financial.services.android.models.dto.quick_shop

import com.google.gson.annotations.SerializedName

data class QuickShopDefaultValues(@SerializedName("foodFulfilmentTypeId") val foodFulfilmentTypeId: Int, @SerializedName("suburb") val suburb: Suburb)