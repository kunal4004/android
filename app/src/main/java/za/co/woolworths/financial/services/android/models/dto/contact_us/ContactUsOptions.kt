package za.co.woolworths.financial.services.android.models.dto.contact_us

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContactUsOptions(
        @SerializedName("key") val key: String?,
        @SerializedName("value") val value: ContactUsValue?,
        @SerializedName("description") val description: String?) : Serializable
