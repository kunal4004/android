package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigOptions(
        @SerializedName("key") val key: String,
        @SerializedName("displayName") val displayName: String,
        @SerializedName("value") val value: String
) : Parcelable