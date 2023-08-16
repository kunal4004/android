package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents.PBDFragmentHeaderContent

@Parcelize
data class PMAPayByDebitOrderEntityItem(
    val content: List<PBDFragmentHeaderContent>?,
    val footer: Footer?,
    val subtitle: String?,
    val title: String?,
) : Parcelable
