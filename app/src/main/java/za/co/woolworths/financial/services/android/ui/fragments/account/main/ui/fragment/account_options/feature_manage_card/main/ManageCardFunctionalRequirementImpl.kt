package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.os.Parcelable
import android.text.TextUtils
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

sealed class StoreCardFeatureType : Parcelable {
    @Parcelize
    data class StoreCardInGoodStanding(
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsActive(
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsPermanentlyBlocked(
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsDefault(
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsTemporaryFreeze(
        var storeCard: StoreCard?,
        var isStoreCardFrozen: Boolean = true,
        var isAnimationEnabled: Boolean = false,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsActivateVirtualTempCardAndIsFreezeCard(
        var storeCard: StoreCard?,
        var isStoreCardFrozen: Boolean = true,
        var isAnimationEnabled: Boolean = false,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()


    @Parcelize
    data class StoreCardIsInstantReplacementCardAndInactive(
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) :
        StoreCardFeatureType()

    @Parcelize
    data class ActivateVirtualTempCard(
        var storeCard: StoreCard?,
        var isTemporaryCardEnabled: Boolean,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    object OnStart : StoreCardFeatureType()

    @Parcelize
    data class TemporaryCardEnabled(
        val isBlockTypeNullInVirtualCardObject: Boolean,
        var storeCard: StoreCard?,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()
    ) : StoreCardFeatureType()

    @Parcelize
    object ManageMyCard : StoreCardFeatureType()
}

enum class StoreCardBlockType(val type: String?) {
    TEMPORARY("temporary"),
    PERMANENT("permanent"),
    NONE("");

    companion object {
        fun getEnum(code: String?): StoreCardBlockType? =
            values().find { it.type == code?.lowercase() }
    }
}

interface IManageCardFunctionalRequirement {
    fun getStoreCardsResponse(): StoreCardsResponse?
    fun isPrimaryCardAvailable(): Boolean
    fun getPrimaryCards(): MutableList<StoreCard>?
    fun getStoreCardData(): StoreCardsData?
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
    fun refreshStoreCardsData()
    fun isOneTimePinUnblockStoreCardEnabled(): Boolean
    fun getCardHolderNameSurname(): String?
    fun getVirtualTempCardNumber(): String
    fun getVirtualTempCardSequence() : Int
}

class ManageCardFunctionalRequirementImpl @Inject constructor(private val accountDao: AccountProductLandingDao) :
    IManageCardFunctionalRequirement {

    private var storeCardData: StoreCardsData? = getStoreCardData()

    override fun refreshStoreCardsData() {
        storeCardData = getStoreCardData()
    }

    override fun isOneTimePinUnblockStoreCardEnabled(): Boolean {
        return getStoreCardsResponse()?.oneTimePinRequired?.unblockStoreCard ?: false
    }

    override fun getCardHolderNameSurname(): String? = KotlinUtils.getCardHolderNameSurname()

    override fun getStoreCardsResponse(): StoreCardsResponse? {
        val response: StoreCardsResponse? = SaveResponseDao.getValue(
            SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD,
            StoreCardsResponse::class.java
        )
        return response?.apply {
            storeCardsData?.productOfferingId = accountDao.product?.productOfferingId.toString()
            storeCardsData?.visionAccountNumber = accountDao.product?.accountNumber.toString()
        }
    }

    // check if primaryCards object exists
    override fun isPrimaryCardAvailable(): Boolean = getPrimaryCards()?.isNotEmpty() ?: false

    override fun getPrimaryCards() = storeCardData?.primaryCards

    override fun getStoreCardData(): StoreCardsData? = getStoreCardsResponse()?.storeCardsData

    override fun getBlockCode(primaryCardIndex: Int) =
        getPrimaryCards()?.elementAt(primaryCardIndex)?.blockCode

    override fun getBlockType(primaryCardIndex: Int): StoreCardBlockType? =
        StoreCardBlockType.getEnum(
            getPrimaryCards()?.elementAt(primaryCardIndex)?.blockType?.lowercase() ?: ""
        )

    override fun getCardNumber(position: Int) = getPrimaryCards()?.get(position)?.number

    override fun getSequenceNumber(position: Int) = getPrimaryCards()?.get(position)?.sequence

    override fun isGenerateVirtualCard(): Boolean = storeCardData?.generateVirtualCard ?: false

    /**
     * Determine if user can generate virtual temp card
     * if generateVirtualCard == TRUE && vtcConfigEnabled == true
     * display Activate Virtual Temp Card
     * display Link New Card
     */
    override fun isActivateVirtualTempCard(): Boolean {
        val isGenerateVirtualCard = isGenerateVirtualCard()
        val isVirtualTempCardFromAppConfigEnabled =
            AppConfigSingleton.virtualTempCard?.isEnabled ?: false
        return isGenerateVirtualCard && isVirtualTempCardFromAppConfigEnabled
    }

    override fun isTemporaryCardEnabled(): Boolean {
        if (storeCardData?.virtualCard != null
            && storeCardData?.virtualCard?.number != null
            && AppConfigSingleton.virtualTempCard?.isEnabled == true
            && (!StoreCardBlockType.PERMANENT.type.equals(
                storeCardData?.virtualCard?.blockType,
                ignoreCase = true
            ))
        ) {
            return true
        }
        return false
    }

    override fun isMultipleStoreCardEnabled(): Boolean = (filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth()?.size ?: 0) > 1

    override fun isStaffMemberAndHasTemporaryCard(): Boolean {
        return isTemporaryCardEnabled()
                && storeCardData?.isStaffMember == true
                && storeCardData?.virtualCardStaffMemberMessage != null
    }

    override fun getVirtualCardStaffMemberMessage(): VirtualCardStaffMemberMessage? {
        return storeCardData?.virtualCardStaffMemberMessage
    }

    /**
     * Display ICR Journey
     * if generateVTC = false && icrConfigEnabled = true && primary card blockType = permanent
     * Show Get replacement
     * display Link new Card
     */
    override fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean {
        if (storeCardData?.primaryCards.isNullOrEmpty()) {
            return false
        }
        val primaryCard = storeCardData?.primaryCards?.get(primaryCardIndex)

        if (storeCardData?.generateVirtualCard == false
            && !TextUtils.isEmpty(primaryCard?.blockType)
            && TemporaryFreezeStoreCard.PERMANENT.equals(primaryCard?.blockType, ignoreCase = true)
            && AppConfigSingleton.instantCardReplacement?.isEnabled == true
        ) {
            return true
        }
        return false
    }

    override fun filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth(): MutableList<StoreCardFeatureType> {

        val primaryCardIndex = 0
        val storeCardResponse = getStoreCardsResponse()
        val virtualCard = storeCardResponse?.storeCardsData?.virtualCard
        val storeCardInPrimaryCardList = storeCardData?.primaryCards?.get(primaryCardIndex)
        val listOfStoreCardFeatures: MutableList<StoreCardFeatureType> = mutableListOf()

        when (val primaryStoreCard = splitStoreCardByCardType(primaryCardIndex, storeCardInPrimaryCardList)) {
            is StoreCardFeatureType.StoreCardIsActivateVirtualTempCardAndIsFreezeCard -> {
                val isBlockTypeTemporary = isBlockTypeTemporary(primaryCardIndex = primaryCardIndex)
                if (isGenerateVirtualCard()){
                   listOfStoreCardFeatures.add(StoreCardFeatureType.ActivateVirtualTempCard(storeCard = primaryStoreCard.storeCard, isBlockTypeTemporary))
                }
                if (isBlockTypeTemporary){
                    listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardIsTemporaryFreeze(storeCard = primaryStoreCard.storeCard, isStoreCardFrozen = true))
                }
            }
            else -> when (isTemporaryCardEnabled()) {
                true -> {
                    val virtualTempCard = StoreCardFeatureType.TemporaryCardEnabled(
                        isBlockTypeNullInVirtualCardObject(),
                        virtualCard
                    )
                    listOfStoreCardFeatures.clear()
                    when (primaryStoreCard) {
                        is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                            if (primaryStoreCard.isStoreCardFrozen) {
                                listOfStoreCardFeatures.add(virtualTempCard)
                                listOfStoreCardFeatures.add(primaryStoreCard)
                            } else {
                                listOfStoreCardFeatures.add(primaryStoreCard)
                                listOfStoreCardFeatures.add(virtualTempCard)
                            }
                        }
                        else -> {
                            listOfStoreCardFeatures.add(virtualTempCard)
                            listOfStoreCardFeatures.add(primaryStoreCard)
                        }
                    }
                }
                false -> {listOfStoreCardFeatures.add(primaryStoreCard)
                }
            }
        }

        return listOfStoreCardFeatures
    }

    override fun getVirtualTempCardNumber() = getVirtualCard()?.number ?: ""
    override fun getVirtualTempCardSequence(): Int  = getVirtualCard()?.sequence ?: -1

    override fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?> {
        return Pair(storeCardData?.virtualCard == null, storeCardData?.virtualCard)
    }

    override fun getVirtualCard() = storeCardData?.virtualCard

    override fun isBlockTypeNullInVirtualCardObject(): Boolean {
        val virtualCard = getVirtualCard()
        return !virtualCard?.blockType.isNullOrEmpty() || StoreCardBlockType.TEMPORARY.type.equals(
            virtualCard?.blockType,
            ignoreCase = true
        )
    }

    override fun isTemporaryFrozenStoreCardAndIsGenerateVirtualTempCardTrue(primaryCardIndex: Int): Boolean {
        return isActivateVirtualTempCard() && isBlockTypeTemporary(primaryCardIndex)
    }

    override fun isBlockTypeTemporary(primaryCardIndex: Int): Boolean {
        return getBlockType(primaryCardIndex) == StoreCardBlockType.TEMPORARY
    }

    /**
     * Is val shouldDisplayStoreCardDetail = TextUtils.isEmpty(blockType) || blockType == TemporaryFreezeStoreCard.TEMPORARY
    val virtualCard = Gson().fromJson(getMyStoreCardDetail(), StoreCardsResponse::class.java)?.storeCardsData?.virtualCard
    // Determine if card is blocked: if blockCode is not null, card is blocked.
    when ((virtualCard != null && AppConfigSingleton.virtualTempCard?.isEnabled == true)
    || shouldDisplayStoreCardDetail
    && blockType != TemporaryFreezeStoreCard.PERMANENT) {
    true -> {temporary store card unfreeze
     */


    override fun isUnFreezeTemporaryStoreCard(primaryCardIndex: Int): Boolean {
        val primaryCard = storeCardData?.primaryCards?.get(primaryCardIndex)
        val blockType = primaryCard?.blockType
        val virtualCard = getVirtualCard()

        return (virtualCard != null && AppConfigSingleton.virtualTempCard?.isEnabled == true)
                || (TextUtils.isEmpty(blockType) || blockType == TemporaryFreezeStoreCard.TEMPORARY) &&
                blockType != TemporaryFreezeStoreCard.PERMANENT
    }

    /**
     * Determine which card to display
     * check if primaryCards object exists
     * Determine if card is ACTIVE or Permanently BLOCKED or Temporarily BLOCKED.
     * If blockCode == null || empty
     * card is ACTIVE
     * display active store card
     * else   blockCode == P
     * card is temporarily blocked
     * display the frozen card according to FREEZE CCARD - Temp Block FRS
     * else if blockCode == L
     * card is permanently blocked
     * display an inactive card
     */
    override fun splitStoreCardByCardType(
        primaryCardIndex: Int,
        storeCard: StoreCard?
    ): StoreCardFeatureType {
        return when {

            isTemporaryFrozenStoreCardAndIsGenerateVirtualTempCardTrue(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsActivateVirtualTempCardAndIsFreezeCard(storeCard)

            isActivateVirtualTempCard() -> StoreCardFeatureType.ActivateVirtualTempCard(storeCard, isBlockTypeTemporary(primaryCardIndex))

            isInstantCardReplacementJourneyEnabled(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive(
                storeCard
            )

            //Unfreeze my card
            isBlockTypeTemporary(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(
                storeCard,
                true
            )

            isUnFreezeTemporaryStoreCard(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(
                storeCard,
                false
            )

            // Manage your card
            else -> StoreCardFeatureType.ManageMyCard
        }
    }
}