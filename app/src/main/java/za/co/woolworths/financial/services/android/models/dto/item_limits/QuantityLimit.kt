package za.co.woolworths.financial.services.android.models.dto.item_limits

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuantityLimit(
    val foodMaximumQuantity: Int?,
    val other: Int?,
    val foodLayoutColour: String?,
    val foodLayoutMessage: String?,
    val allowsCheckout: Boolean?,
    val otherLayoutColour: String?,
    val food: Int?,
    val otherMaximumQuantity: Int?
) : Parcelable
