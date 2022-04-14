package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.DetermineCardToDisplay

@Parcelize
data class StoreCard(
    val holderType: String?,
    val type: String?,
    val expiryDate: String?,
    val daysUntilExpiry: String?,
    val idRequired: Boolean?,
    val sequence: String,
    val embossedName: String?,
    val usage: String?,
    var blockCode: String? = "",
    val number: String,
    val dateOpened: String,
    val dateLastMaintained: String,
    var blockType: String? = "",
    var cardDisplay: DetermineCardToDisplay? = DetermineCardToDisplay.StoreCardIsDefault
):Parcelable