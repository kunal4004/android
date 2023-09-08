package za.co.woolworths.financial.services.android.recommendations.data.response.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CartProducts(
    val pid: String?,
    var quantity: Int = 0,
    val value: String?,
    val sku: String?,
    val currency: String?,
) : Parcelable