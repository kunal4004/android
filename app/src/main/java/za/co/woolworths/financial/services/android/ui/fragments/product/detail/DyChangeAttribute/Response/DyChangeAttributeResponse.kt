package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DyChangeAttributeResponse(
    val httpCode: Int,
    val response: Response
): Parcelable