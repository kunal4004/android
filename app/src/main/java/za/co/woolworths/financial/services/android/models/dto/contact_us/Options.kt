package za.co.woolworths.financial.services.android.models.dto.contact_us

import com.google.gson.annotations.SerializedName

data class Options(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String)