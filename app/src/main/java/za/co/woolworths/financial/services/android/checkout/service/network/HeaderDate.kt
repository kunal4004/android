package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.annotations.SerializedName

class HeaderDate {
    @SerializedName("dayInitial")
    var dayInitial: String? = null

    @SerializedName("date")
    var date: String? = null
}