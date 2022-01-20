package za.co.woolworths.financial.services.android.models.dto.app_config.contact_us

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions

@Parcelize
data class ConfigContactUsValue(
    @SerializedName("call") val call: ConfigContactUsCall?,
    @SerializedName("email") val email: List<ConfigOptions>?,
    @SerializedName("faxNumber") var faxNumber: String? = ""
) : Parcelable