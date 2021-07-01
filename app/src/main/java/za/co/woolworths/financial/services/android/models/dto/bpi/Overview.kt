package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Overview(
    val benefits: List<Int>,
    val description: Int,
    val header:Int? = null,
    val title: Int
) : Parcelable