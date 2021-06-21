package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DefaultLabel(
    val howToClaim: String,
    val requiredDocuments: String,
    val claimReasonTitle: String,
    val overviewTitle: String
) : Parcelable