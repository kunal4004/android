package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class SubstitutionInfo(
    @SerializedName("displayName")
    val displayName: String,
    val substitutionSelection: String? = null,
    val isSubstitutionInStock: Boolean = false,
    @SerializedName("id")
    val id: String = "",
    val substitutionId: String? = null
): Parcelable, Serializable