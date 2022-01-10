package za.co.woolworths.financial.services.android.models.dto.app_config.contact_us

import com.google.gson.annotations.SerializedName

data class ConfigContactUsEmail(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String
)