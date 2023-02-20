package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductX(
    val productId: String?,
) : Parcelable