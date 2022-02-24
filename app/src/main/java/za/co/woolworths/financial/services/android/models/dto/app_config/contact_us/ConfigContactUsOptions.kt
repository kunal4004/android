package za.co.woolworths.financial.services.android.models.dto.app_config.contact_us

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class ConfigContactUsOptions(
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: ConfigContactUsValue?,
    @SerializedName("description") val description: String?
) : Parcelable, Serializable
