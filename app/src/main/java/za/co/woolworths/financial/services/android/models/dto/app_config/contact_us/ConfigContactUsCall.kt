package za.co.woolworths.financial.services.android.models.dto.app_config.contact_us

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions

@Parcelize
data class ConfigContactUsCall(
        @SerializedName("operatingHours") val operatingHours: String?,
        @SerializedName("options") val options: ArrayList<ConfigOptions>?
) : Parcelable