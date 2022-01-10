package za.co.woolworths.financial.services.android.models.dto.app_config.contact_us

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigContactUs(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String,
        @SerializedName("options") val options: ArrayList<ConfigContactUsOptions>
) : Parcelable