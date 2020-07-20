package za.co.woolworths.financial.services.android.models.dto.contact_us

import com.google.gson.annotations.SerializedName

data class ContactUs(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String,
        @SerializedName("options") val options: ArrayList<ContactUsOptions>
)