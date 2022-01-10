package za.co.woolworths.financial.services.android.models.dto.app_config.whatsapp

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigShowWhatsAppIcon(
        @SerializedName("contactUsFinancialServices") val contactUsFinancialServices: Boolean,
        @SerializedName("ccPaymentOptions") val ccPaymentOptions: Boolean,
        @SerializedName("scPaymentOptions") val scPaymentOptions: Boolean,
        @SerializedName("plPaymentOptions") var plPaymentOptions: Boolean,
        @SerializedName("plLanding") val plLanding: Boolean,
        @SerializedName("ccLandingPage") val ccLandingPage: Boolean,
        @SerializedName("scLandingPage") val scLandingPage: Boolean
) : Parcelable