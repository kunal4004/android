package za.co.woolworths.financial.services.android.models.dto.whatsapp

import com.google.gson.annotations.SerializedName

data class ShowWhatsAppIcon(

        @SerializedName("contactUsFinancialServices") val contactUsFinancialServices: Boolean,
        @SerializedName("ccPaymentOptions") val ccPaymentOptions: Boolean,
        @SerializedName("plLanding") val plLanding: Boolean,
        @SerializedName("ccLandingPage") val ccLandingPage: Boolean,
        @SerializedName("scLandingPage") val scLandingPage: Boolean
)