package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoreCardsData (
    val generateVirtualCard: Boolean = false,
    val virtualCard: StoreCard?,
    val isStaffMember: Boolean,
    val virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?,
    var primaryCards: MutableList<StoreCard>?,
    val secondaryCards: List<StoreCard>?) : Parcelable
{
    var visionAccountNumber: String = ""
    var productOfferingId: String = ""
}