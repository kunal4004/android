package za.co.woolworths.financial.services.android.enhancedSubstitution.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SubstitutionInfo(
        @SerializedName("displayName")
        @Expose
        val displayName: String,
        @SerializedName("id")
        @Expose
        val id: String
)