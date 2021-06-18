package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Overview(
    val benefits: List<String>,
    val description: String,
    val title: String
) : Parcelable