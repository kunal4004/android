package za.co.woolworths.financial.services.android.models.dto.whatsapp

import com.google.gson.annotations.SerializedName

data class WhatsApp(
        @SerializedName("baseUrl") val baseUrl: String,
        @SerializedName("phoneNumber") val phoneNumber: Int,
        @SerializedName("text") val text: String,
        @SerializedName("showWhatsAppIcon") val showWhatsAppIcon: ShowWhatsAppIcon,
        @SerializedName("availabilityTimes") val availabilityTimes: AvailabilityTimes,
        @SerializedName("minimumSupportedAppBuildNumber") val minimumSupportedAppBuildNumber: Int
)