package za.co.woolworths.financial.services.android.enhancedSubstitution.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubstitutionInfo(
    @SerializedName("displayName")
    val displayName: String,
    val substitutionSelection: String?,
    val isSubstitutionInStock: Boolean = false,
    @SerializedName("id")
    val id: String,
    val substitutionId: String?
): Parcelable