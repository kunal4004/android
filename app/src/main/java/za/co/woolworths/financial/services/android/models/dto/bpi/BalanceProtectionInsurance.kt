package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BalanceProtectionInsurance(
    val claimReason: MutableList<ClaimReason>,
    val defaultLabel: DefaultLabel,
    val overview: MutableList<Overview>
):Parcelable