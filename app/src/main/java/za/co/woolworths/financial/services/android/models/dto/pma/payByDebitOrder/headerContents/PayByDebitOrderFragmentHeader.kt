package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PayByDebitOrderFragmentHeader(
    val headerContent: List<PBDFragmentHeaderContent>?,
    val title: String,
) : Parcelable
