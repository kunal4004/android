package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SubmitClaimReason(
     val description: Int,
    val requiredForm: List<Int>,
    val requiredSubmit: List<Int>,
    val title: Int
): Parcelable