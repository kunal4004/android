package za.co.woolworths.financial.services.android.models.dto.quick_shop

import com.google.gson.annotations.SerializedName

data class FulfilmentTypes(@SerializedName("fulfilmentStoreId") val fulfilmentStoreId: Int, @SerializedName("fulfilmentTypeId") val fulfilmentTypeId: String)