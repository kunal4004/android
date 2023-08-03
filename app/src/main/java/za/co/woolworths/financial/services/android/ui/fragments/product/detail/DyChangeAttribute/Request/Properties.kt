package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Properties(
    val attributeType: String? = null,
    val attributeValue: String? = null,
    val dyType: String? = null,
    val keywords: String? = null,
    val value: String? = null,
    val currency: String? = null,
    val quantity: Int? = null,
    val productId: String? = null,
    val color: String? = null,
    val sku_id: String? = null,
    val hashedEmail: String? = null,
    val size: String? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val filterType: String? = null,
    val filterStringValue: String? = null,
    val uniqueTransactionId: String? = null,
    val cart: List<Cart>? = null
): Parcelable