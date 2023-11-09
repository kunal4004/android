package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cart(
    val productId: String? = null,
    val quantity: Int? = null,
    val itemPrice: String? = null
): Parcelable
