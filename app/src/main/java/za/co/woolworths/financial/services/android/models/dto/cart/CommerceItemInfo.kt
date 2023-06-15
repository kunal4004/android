package za.co.woolworths.financial.services.android.models.dto.cart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommerceItemInfo(
    var quantity: Int = 0,
    var productId: String = "",
    var color: String = "",
    var primarySize: String = "",
    var internalSwatchImage: String = "",
    var isGWP: Boolean? = null,
    var internalImageURL: String = "",
    var commerceItemClassType: String = "",
    var catalogRefId: String = "",
    var size: String = "",
    var productDisplayName: String = "",
    var externalImageRefV2: String = "",
    var id: String = ""
) : Parcelable