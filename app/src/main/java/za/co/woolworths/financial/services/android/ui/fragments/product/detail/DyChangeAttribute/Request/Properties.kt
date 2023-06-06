package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Properties(
    val attributeType: String? = null,
    val attributeValue: String? = null,
    val dyType: String? = null,
    val keywords: String? = null
): Parcelable