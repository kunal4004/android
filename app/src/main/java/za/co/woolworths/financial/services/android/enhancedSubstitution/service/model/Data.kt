package za.co.woolworths.financial.services.android.enhancedSubstitution.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

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