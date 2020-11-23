package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName

data class PMACard(
        @SerializedName("number") val number: String,
        @SerializedName("name_card") val name_card: String,
        @SerializedName("exp_month") val exp_month: String,
        @SerializedName("exp_year") val exp_year: String,
        @SerializedName("cvv") val cvv: String,
        @SerializedName("payer_id") val payer_id: Int,
        @SerializedName("method") val method: String,
        @SerializedName("type") val type: String)