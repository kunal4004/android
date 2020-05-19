package za.co.woolworths.financial.services.android.models.dto.whatsapp

import com.google.gson.annotations.SerializedName

data class AvailabilityTimes(

        @SerializedName("startTime") val startTime: String,
        @SerializedName("endTime") val endTime: String
)