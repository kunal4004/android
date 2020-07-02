package za.co.woolworths.financial.services.android.models.dto.contact_us

import com.google.gson.annotations.SerializedName

data class ContactUsValue(
        @SerializedName("call") val call: ContactUsCall?,
        @SerializedName("email") val email: List<Options>?,
        @SerializedName("faxNumber") var faxNumber: String? = "")