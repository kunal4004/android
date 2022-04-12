package za.co.woolworths.financial.services.android.util.eliteplan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ElitePlanModel(val scope: String?,val discountAmount: String?,val settlementAmount: String?) :
    Parcelable