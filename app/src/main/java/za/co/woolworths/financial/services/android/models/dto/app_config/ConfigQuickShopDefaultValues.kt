package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.Suburb

@Parcelize
data class ConfigQuickShopDefaultValues(
    @SerializedName("foodFulfilmentTypeId") val foodFulfilmentTypeId: String,
    @SerializedName("digitalProductsFulfilmentTypeId") val digitalProductsFulfilmentTypeId: String,
    @SerializedName("suburb") val suburb: ConfigSuburb
) : Parcelable