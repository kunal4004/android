package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Province(
    var id: String? = null,
    var name: String? = null
) : Parcelable
