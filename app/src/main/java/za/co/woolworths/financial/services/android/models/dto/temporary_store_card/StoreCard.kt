package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoreCard(
    val holderType: String?,
    val type: String?,
    val expiryDate: String?,
    val daysUntilExpiry: String?,
    val idRequired: Boolean?,
    val sequence: Int,
    val embossedName: String?,
    val usage: String?,
    var blockCode: String? = "",
    val number: String,
    val dateOpened: String,
    val dateLastMaintained: String,
    var blockType: String? = "",
    var cardNotReceived: Boolean = false
):Parcelable