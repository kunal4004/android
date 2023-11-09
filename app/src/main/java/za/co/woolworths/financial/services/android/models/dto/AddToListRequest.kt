package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddToListRequest(
    var skuID: String? = null,
    var giftListId: String? = null,
    var catalogRefId: String? = null,
    var quantity: String? = null,
    var listId: String? = null,
    var isGWP: Boolean = false,
    var size: String? = null
): Parcelable