package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage


interface IManageCardFunctionalRequirement {
    fun getStoreCardsResponse(): StoreCardsResponse?
    fun isPrimaryCardAvailable(): Boolean
    fun getPrimaryCards(): MutableList<StoreCard>?
    fun splitStoreCardByCardType(
        primaryCardIndex: Int,
        storeCard: StoreCard?
    ): StoreCardFeatureType

    fun getBlockCode(primaryCardIndex: Int): String?
    fun getBlockType(primaryCardIndex: Int): StoreCardBlockType?
    fun getCardNumber(position: Int): String?
    fun getSequenceNumber(position: Int): Int?
    fun isGenerateVirtualCard(): Boolean
    fun isActivateVirtualTempCard(): Boolean
    fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean
    fun filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth(): MutableList<StoreCardFeatureType>?
    fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?>
    fun getVirtualCard(): StoreCard?
    fun isBlockTypeNullInVirtualCardObject(): Boolean
    fun isTemporaryFrozenStoreCardAndIsGenerateVirtualTempCardTrue(primaryCardIndex: Int): Boolean
    fun isBlockTypeTemporary(primaryCardIndex: Int): Boolean
    fun isUnFreezeTemporaryStoreCard(primaryCardIndex: Int): Boolean
    fun isTemporaryCardEnabled(): Boolean
    fun isMultipleStoreCardEnabled(): Boolean
    fun isStaffMemberAndHasTemporaryCard(): Boolean
    fun getVirtualCardStaffMemberMessage(): VirtualCardStaffMemberMessage?
    fun isOneTimePinUnblockStoreCardEnabled(): Boolean
    fun getCardHolderNameSurname(): String?
    fun getVirtualTempCardNumber(): String
    fun getVirtualTempCardSequence(): Int
    fun isUpshellCardForFreezeStoreCard(): Boolean
    fun isUpshellCardForActivateVirtualCard(): Boolean
    fun calculateUpShellMessage(storeCard: StoreCard?): StoreCardUpShellMessage
}
