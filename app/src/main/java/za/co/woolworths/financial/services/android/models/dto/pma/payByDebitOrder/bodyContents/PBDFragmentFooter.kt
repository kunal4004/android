package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PBDFragmentFooter(
    val text: String,
) : Parcelable
