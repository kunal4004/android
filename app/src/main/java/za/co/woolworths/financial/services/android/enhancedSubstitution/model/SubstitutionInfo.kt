package za.co.woolworths.financial.services.android.enhancedSubstitution.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubstitutionInfo(
    val displayName: String,
    val substitutionSelection: String?,
    val isSubstitutionInStock: Boolean = false,
    val id: String,
    val substitutionId: String?
): Parcelable