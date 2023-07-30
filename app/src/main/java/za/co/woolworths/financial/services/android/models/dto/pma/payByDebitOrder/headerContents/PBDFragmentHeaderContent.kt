package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PBDFragmentHeaderContent(
    val email: String?,
    val phone: String?,
    val text: String?,
) : Parcelable
