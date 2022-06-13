package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.os.Parcelable
import android.text.TextUtils
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import javax.inject.Inject

sealed class StoreCardFeatureType : Parcelable {
    @Parcelize
    data class StoreCardInGoodStanding(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsActive(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsPermanentlyBlocked(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsDefault(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsTemporaryFreeze(
        var storeCard: StoreCard?,
        var isStoreCardFrozen: Boolean = true,
        var isAnimationEnabled : Boolean = false
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsInstantReplacementCardAndInactive(var storeCard: StoreCard?) :
        StoreCardFeatureType()

    @Parcelize
    data class ActivateVirtualTempCard(var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    object OnStart : StoreCardFeatureType()

    @Parcelize
    data class TemporaryCardEnabled(
        val isBlockTypeNullInVirtualCardObject: Boolean,
        var storeCard: StoreCard?
    ) : StoreCardFeatureType()

    @Parcelize
    object ManageMyCard : StoreCardFeatureType()
}

enum class StoreCardType(val type: String) {
    TEMPORARY("p"),
    PERMANENT("l")
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
    fun splitStoreCardByCardType(primaryCardIndex: Int, storeCard: StoreCard?): StoreCardFeatureType
    fun getBlockCode(primaryCardIndex: Int): String?
    fun getBlockType(primaryCardIndex: Int): StoreCardBlockType?
    fun getCardNumber(position: Int): String?
    fun getSequenceNumber(position: Int): Int?
    fun isGenerateVirtualCard(): Boolean
    fun isActivateVirtualTempCard(): Boolean
    fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean
    fun addManageMyCardsLinkOnStoreCardLandingScreen()
    fun getStoreCardListByFeatureType(): MutableList<StoreCardFeatureType>?
    fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?>
    fun getVirtualCard(): StoreCard?
    fun isBlockTypeNullInVirtualCardObject(): Boolean
    fun isFreezeStoreCard(primaryCardIndex: Int): Boolean
    fun isUnFreezeTemporaryStoreCard(primaryCardIndex: Int): Boolean
    fun isTemporaryCardEnabled(): Boolean
}

class ManageCardFunctionalRequirementImpl @Inject constructor(private val accountDao: AccountProductLandingDao) :
    IManageCardFunctionalRequirement {

    override fun getStoreCardsResponse(): StoreCardsResponse? {
        val response = SaveResponseDao.getValue(
            SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD,
            StoreCardsResponse::class.java
        )
        return response.apply {
            storeCardsData?.productOfferingId = accountDao.product?.productOfferingId.toString()
            storeCardsData?.visionAccountNumber = accountDao.product?.accountNumber.toString()
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

    override fun getCardNumber(position: Int) = getPrimaryCards()?.get(position)?.number

    override fun getSequenceNumber(position: Int) = getPrimaryCards()?.get(position)?.sequence

    override fun isGenerateVirtualCard(): Boolean = getStoreCardData()?.generateVirtualCard ?: false

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
        val response = getStoreCardsResponse()
        if (response?.storeCardsData?.virtualCard != null
            && response.storeCardsData?.virtualCard?.number != null
            && (!TemporaryFreezeStoreCard.PERMANENT.equals(
                response.storeCardsData?.virtualCard?.blockType,
                ignoreCase = true
            ))
        ) {
            return true
        }
        return false
    }

    /**
     * Display ICR Journey
     * if generateVTC = false && icrConfigEnabled = true && primary card blockType = permanent
     * Show Get replacement
     * display Link new Card
     */
    override fun isInstantCardReplacementJourneyEnabled(primaryCardIndex: Int): Boolean {
        val response = getStoreCardsResponse()
        if (response?.storeCardsData?.primaryCards.isNullOrEmpty()) {
            return false
        }
        val primaryCard = response?.storeCardsData?.primaryCards?.get(primaryCardIndex)

        if (response?.storeCardsData?.generateVirtualCard == false
            && !TextUtils.isEmpty(primaryCard?.blockType)
            && TemporaryFreezeStoreCard.PERMANENT.equals(primaryCard?.blockType, ignoreCase = true)
            && AppConfigSingleton.instantCardReplacement?.isEnabled == true
        ) {
            return true
        }
        return false
    }

    override fun addManageMyCardsLinkOnStoreCardLandingScreen() {

    }

    override fun getStoreCardListByFeatureType(): MutableList<StoreCardFeatureType>? {
        val primaryCards = getStoreCardsResponse()?.storeCardsData?.primaryCards
        val listOfStoreCardFeatures = mutableListOf<StoreCardFeatureType>()
        primaryCards?.forEachIndexed { index, storeCard ->
            val card = splitStoreCardByCardType(index, storeCard)
            listOfStoreCardFeatures.add(card)
        }
        return listOfStoreCardFeatures
    }

    override fun isVirtualCardObjectExist(): Pair<Boolean, StoreCard?> {
        val storeCardData = getStoreCardData()
        return Pair(storeCardData?.virtualCard == null, storeCardData?.virtualCard)
    }

    override fun getVirtualCard() = getStoreCardData()?.virtualCard

    override fun isBlockTypeNullInVirtualCardObject(): Boolean {
        val virtualCard = getVirtualCard()
        return !virtualCard?.blockType.isNullOrEmpty() || StoreCardBlockType.TEMPORARY.type.equals(
            virtualCard?.blockType,
            ignoreCase = true
        )
    }

    override fun isFreezeStoreCard(primaryCardIndex: Int): Boolean {
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
        val primaryCard = getStoreCardData()?.primaryCards?.get(primaryCardIndex)
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
            isActivateVirtualTempCard() -> StoreCardFeatureType.ActivateVirtualTempCard(storeCard)

            isTemporaryCardEnabled() -> StoreCardFeatureType.TemporaryCardEnabled(
                isBlockTypeNullInVirtualCardObject = isBlockTypeNullInVirtualCardObject(),
                storeCard = storeCard
            )

            isInstantCardReplacementJourneyEnabled(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive(
                storeCard
            )

            //Unfreeze my card
            isFreezeStoreCard(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(
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