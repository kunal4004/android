package za.co.woolworths.financial.services.android.models.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddCardResponse(
        @SerializedName("token") val token: String,
        @SerializedName("card") val card: PMACard,
        @SerializedName("saveChecked")  var saveChecked: Boolean = false
) : Serializable