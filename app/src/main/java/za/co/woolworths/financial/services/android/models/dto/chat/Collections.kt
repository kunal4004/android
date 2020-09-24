package za.co.woolworths.financial.services.android.models.dto.chat

import com.google.gson.annotations.SerializedName

data class Collections(
        @SerializedName("offlineMessageTemplate") val offlineMessageTemplate: String,
        @SerializedName("emailAddress") val emailAddress: String,
        @SerializedName("emailSubjectLine") val emailSubjectLine: String,
        @SerializedName("emailMessage") val emailMessage: String
)