package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import javax.inject.Inject

sealed class StoreCardFeatureType : Parcelable {
    @Parcelize
    data class StoreCardInGoodStanding(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsActive(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsTemporaryFreeze(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsPermanentlyBlocked(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsInstantReplacementCardAndInactive(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsDefault(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class ActivateVirtualTempCard(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    object OnStart : StoreCardFeatureType()
}

enum class StoreCardType(val type: String) {
    TEMPORARY("p"),
    PERMANENT("l")
}

enum class StoreCardBlockType(private val type: String?) {
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
    fun determineCardDisplay(primaryCardIndex: Int, storeCard: StoreCard?): StoreCardFeatureType
    fun getBlockCode(primaryCardIndex: Int): String?
    fun getBlockType(primaryCardIndex: Int): StoreCardBlockType?
    fun isGenerateVirtualCard(): Boolean
    fun canUserGenerateVirtualTempCard(primaryCardIndex: Int): Boolean
    fun isTemporaryCardEnabled(primaryCardIndex: Int): Boolean
    fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean
    fun addManageMyCardsLinkOnStoreCardLandingScreen()
    fun getStoreCardListByFeatureType(): MutableList<StoreCardFeatureType>?
    fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?>
}

class ManageCardFunctionalRequirementImpl @Inject constructor() : IManageCardFunctionalRequirement {

    override fun getStoreCardsResponse(): StoreCardsResponse? = SaveResponseDao.getValue(
        SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD,
        StoreCardsResponse::class.java
    )

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
    override fun determineCardDisplay(
        primaryCardIndex: Int,
        storeCard: StoreCard?
    ): StoreCardFeatureType {
        val blockCode = getBlockCode(primaryCardIndex)?.lowercase()
        return when (isPrimaryCardAvailable()) {
            true -> when {
                blockCode.isNullOrEmpty() -> StoreCardFeatureType.StoreCardIsActive(storeCard)
                blockCode.equals(
                    StoreCardType.TEMPORARY.type,
                    ignoreCase = true
                ) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(storeCard)
                blockCode.equals(StoreCardType.PERMANENT.type, ignoreCase = true) -> when {
                    isInstantCardReplacementJourneyEnabled(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive(
                        storeCard
                    )
                    canUserGenerateVirtualTempCard(primaryCardIndex) -> StoreCardFeatureType.ActivateVirtualTempCard(
                        storeCard
                    )

                    else -> StoreCardFeatureType.StoreCardIsDefault(storeCard)
                }
                else -> StoreCardFeatureType.StoreCardIsDefault(storeCard)
            }
            false -> StoreCardFeatureType.StoreCardIsDefault(storeCard)
        }
    }

    // check if primaryCards object exists
    override fun isPrimaryCardAvailable(): Boolean = getPrimaryCards()?.isNotEmpty() ?: false

    override fun getPrimaryCards() = getStoreCardData()?.primaryCards

    override fun getStoreCardData(): StoreCardsData? = getStoreCardsResponse()?.storeCardsData

    override fun getBlockCode(primaryCardIndex: Int) =
        getPrimaryCards()?.elementAt(primaryCardIndex)?.blockCode

    override fun getBlockType(primaryCardIndex: Int): StoreCardBlockType? =
        StoreCardBlockType.getEnum(
            getPrimaryCards()?.elementAt(primaryCardIndex)?.blockType?.lowercase() ?: ""
        )

    override fun isGenerateVirtualCard(): Boolean = getStoreCardData()?.generateVirtualCard ?: false

    /**
     * Determine if user can generate virtual temp card
     * if generateVirtualCard == TRUE && vtcConfigEnabled == true
     * display Activate Virtual Temp Card
     * display Link New Card
     */
    override fun canUserGenerateVirtualTempCard(primaryCardIndex: Int): Boolean {
        val isGenerateVirtualCard = isGenerateVirtualCard()
        val isVirtualTempCardFromAppConfigEnabled =
            AppConfigSingleton.virtualTempCard?.isEnabled ?: false
        return isGenerateVirtualCard && isVirtualTempCardFromAppConfigEnabled
    }

    override fun isTemporaryCardEnabled(primaryCardIndex: Int): Boolean {
        val storeCardData = getStoreCardData()
        val isVirtualCardNotNull = storeCardData?.virtualCard != null
        val isVirtualCardNumberNotNull = storeCardData?.virtualCard?.number != null
        val blockType = getBlockType(primaryCardIndex)
        return blockType == StoreCardBlockType.PERMANENT && isVirtualCardNotNull && isVirtualCardNumberNotNull
    }

    /**
     * Display ICR Journey
     * if generateVTC = false && icrConfigEnabled = true && primary card blockType = permanent
     * Show Get replacement
     * display Link new Card
     */
    override fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean {
        val isGenerateVirtualCard = !isGenerateVirtualCard()
        val blockType = getBlockType(primaryCardIndex)
        val isInstantCardReplacementFromAppConfigEnabled =
            AppConfigSingleton.instantCardReplacement?.isEnabled ?: false
        return blockType != StoreCardBlockType.NONE
                && blockType == StoreCardBlockType.PERMANENT
                && isInstantCardReplacementFromAppConfigEnabled
                && isGenerateVirtualCard
    }

    override fun addManageMyCardsLinkOnStoreCardLandingScreen() {

    }

    override fun getStoreCardListByFeatureType(): MutableList<StoreCardFeatureType>? {
        val primaryCards = getStoreCardsResponse()?.storeCardsData?.primaryCards
        val listOfStoreCardFeatures = mutableListOf<StoreCardFeatureType>()
        primaryCards?.forEachIndexed { index, storeCard ->
            val card = determineCardDisplay(index, storeCard)
            listOfStoreCardFeatures.add(card)
        }
        return listOfStoreCardFeatures
    }

    override fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?> {
        val storeCardData = getStoreCardData()
        return Pair(storeCardData?.virtualCard == null, storeCardData?.virtualCard)
    }
}