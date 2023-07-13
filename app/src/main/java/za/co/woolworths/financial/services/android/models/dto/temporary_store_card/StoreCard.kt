package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
    @SerializedName("cardNotReceived")
    var isCardNotReceivedFlowNeeded: Boolean = false,
    var actions: List<ActionButton>?
):Parcelable


@Parcelize
data class ActionButton(var label: String?, var action : StoreCardItemActions?) : Parcelable

enum class StoreCardItemActions (action: String?){
    LINK_STORE_CARD("LINK_STORE_CARD"),
    ACTIVATE_VIRTUAL_CARD("ACTIVATE_VIRTUAL_CARD"),
    CARD_REPLACEMENT("CARD_REPLACEMENT"),
    PAY_WITH_CARD("PAY_WITH_CARD"),
    HOW_IT_WORKS("HOW_IT_WORKS")
}