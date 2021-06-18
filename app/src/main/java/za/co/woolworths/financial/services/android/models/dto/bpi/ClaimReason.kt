package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ClaimReason(
    val description: String,
    val requiredForm: List<String>,
    val requiredSubmit: List<String>,
    val title: String
): Parcelable