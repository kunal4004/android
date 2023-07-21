package za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Footer(
    val text: String,
) : Parcelable
