package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigFulfilmentTypes(
    @SerializedName("fulfilmentStoreId") val fulfilmentStoreId: Int,
    @SerializedName("fulfilmentTypeId") val fulfilmentTypeId: String
) : Parcelable