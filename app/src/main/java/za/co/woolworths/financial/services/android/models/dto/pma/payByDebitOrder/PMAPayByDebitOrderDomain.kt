package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.bodyContents.PayByDebitOrderFragmentBody
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.headerContents.PayByDebitOrderFragmentHeader

@Parcelize
data class PMAPayByDebitOrderDomain(
    var header: PayByDebitOrderFragmentHeader,
    var body: PayByDebitOrderFragmentBody,
) : Parcelable
