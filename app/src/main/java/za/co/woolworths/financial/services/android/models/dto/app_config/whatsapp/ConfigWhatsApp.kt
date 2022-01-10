package za.co.woolworths.financial.services.android.models.dto.app_config.whatsapp

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigAvailabilityTimes

@Parcelize
data class ConfigWhatsApp(
        var showWhatsAppButton:Boolean= false,
        @SerializedName("baseUrl") val baseUrl: String,
        @SerializedName("phoneNumber") val phoneNumber: String,
        @SerializedName("text") val text: String,
        @SerializedName("showWhatsAppIcon") var showWhatsAppIcon: ConfigShowWhatsAppIcon,
        @SerializedName("availabilityTimes") val availabilityTimes: ConfigAvailabilityTimes,
        @SerializedName("minimumSupportedAppBuildNumber") val minimumSupportedAppBuildNumber: Int
) : Parcelable