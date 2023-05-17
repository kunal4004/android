package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("links")
        @Expose
        val links: List<Any>,
        @SerializedName("substitutionInfo")
        @Expose
        val substitutionInfo: SubstitutionInfo,
        @SerializedName("substitutionSelection")
        @Expose
        val substitutionSelection: String
)