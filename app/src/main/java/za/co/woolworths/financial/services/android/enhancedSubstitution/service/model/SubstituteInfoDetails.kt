package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SubstituteInfoDetails(
        @SerializedName("commerceItemId")
        @Expose
        var commerceItemId: String?,
        @SerializedName("substitutionId")
        @Expose
        var substitutionId: String?,
        @SerializedName("sustitutionDisplayName")
        @Expose
        var sustitutionDisplayName: String?,
        @SerializedName("parentProductId")
        @Expose
        var parentProductId: String?,
        @SerializedName("substitutionSelection")
        @Expose
        var substitutionSelection: String?
)