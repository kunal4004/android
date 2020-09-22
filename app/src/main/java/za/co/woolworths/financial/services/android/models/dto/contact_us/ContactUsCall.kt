package za.co.woolworths.financial.services.android.models.dto.contact_us

import com.google.gson.annotations.SerializedName

data class ContactUsCall(
        @SerializedName("operatingHours") val operatingHours: String?,
        @SerializedName("options") val options: ArrayList<Options>?
)