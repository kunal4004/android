package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

data class StoreCardsData (
    val generateVirtualCard: Boolean = false,
    val virtualCard: StoreCard?,
    val isStaffMember: Boolean,
    val virtualCardStaffMemberMessage: VirtualCardStaffMemberMessage?,
    val primaryCards: List<StoreCard>?,
    val secondaryCards: List<StoreCard>?)
{
    var visionAccountNumber: String = ""
    var productOfferingId: String = ""
}