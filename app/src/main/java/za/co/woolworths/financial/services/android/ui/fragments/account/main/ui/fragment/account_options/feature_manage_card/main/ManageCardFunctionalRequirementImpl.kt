package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.text.TextUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.VirtualCardStaffMemberMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

class ManageCardFunctionalRequirementImpl @Inject constructor(private val accountDao: AccountProductLandingDao) :
    IManageCardFunctionalRequirement {

    private var storeCardData: StoreCardsData? = null
    get() = getStoreCardsResponse()?.storeCardsData

    override fun isOneTimePinUnblockStoreCardEnabled(): Boolean {
        return getStoreCardsResponse()?.oneTimePinRequired?.unblockStoreCard ?: false
    }

    override fun getCardHolderNameSurname(): String? = KotlinUtils.getCardHolderNameSurname()

    override fun getStoreCardsResponse(): StoreCardsResponse? {
        val response: StoreCardsResponse? = accountDao.storeCardsData
        return response?.apply {
            storeCardsData?.productOfferingId = accountDao.product?.productOfferingId.toString()
            storeCardsData?.visionAccountNumber = accountDao.product?.accountNumber.toString()
        }
    }

    // check if primaryCards object exists
    override fun isPrimaryCardAvailable(): Boolean = getPrimaryCards()?.isNotEmpty() ?: false

    override fun getPrimaryCards() = storeCardData?.primaryCards

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

    override fun isMultipleStoreCardEnabled(): Boolean = (filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth().size ) > 1

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
        val listOfStoreCardFeatures: MutableList<StoreCardFeatureType> = mutableListOf()
        val storeCardResponse = getStoreCardsResponse()
        val virtualCard = storeCardResponse?.storeCardsData?.virtualCard
        val primaryCards :  MutableList<StoreCard>? = storeCardData?.primaryCards

        if (primaryCards?.isEmpty() == true && virtualCard == null){
            return mutableListOf()
        }
        val storeCardInPrimaryCardList = primaryCards?.get(primaryCardIndex)

        val virtualTempCard = StoreCardFeatureType.TemporaryCardEnabled(
            isBlockTypeNullInVirtualCardObject(),
            virtualCard
        )

        when (val primaryStoreCard = splitStoreCardByCardType(primaryCardIndex, storeCardInPrimaryCardList)) {
            is StoreCardFeatureType.StoreCardIsActivateVirtualTempCardAndIsFreezeCard -> {
                val isBlockTypeTemporary = isBlockTypeTemporary(primaryCardIndex = primaryCardIndex)
                when(primaryStoreCard.upShellMessage){
                    is StoreCardUpShellMessage.ActivateVirtualTempCard -> {
                        if (isBlockTypeTemporary){
                            listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardIsTemporaryFreeze(storeCard = primaryStoreCard.storeCard, isStoreCardFrozen = true, upShellMessage = calculateUpShellMessage(primaryStoreCard.storeCard)))
                            listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage(primaryStoreCard.storeCard))
                        }
                    }

                    else -> {
                        if (isGenerateVirtualCard()){
                            listOfStoreCardFeatures.add(StoreCardFeatureType.ActivateVirtualTempCard(storeCard = primaryStoreCard.storeCard, upShellMessage = calculateUpShellMessage(primaryStoreCard.storeCard), isTemporaryCardEnabled = isBlockTypeTemporary))
                        }
                        if (isBlockTypeTemporary){
                            listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardIsTemporaryFreeze(storeCard = primaryStoreCard.storeCard, isStoreCardFrozen = true, upShellMessage = calculateUpShellMessage(primaryStoreCard.storeCard)))
                        }
                    }
                }
            }
            else -> when (isTemporaryCardEnabled()) {
                true -> {

                    listOfStoreCardFeatures.clear()
                    when (primaryStoreCard) {
                        is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                            if (primaryStoreCard.isStoreCardFrozen) {
                                listOfStoreCardFeatures.add(virtualTempCard)
                                listOfStoreCardFeatures.add(primaryStoreCard)
                            } else {
                                listOfStoreCardFeatures.add(virtualTempCard)
                                listOfStoreCardFeatures.add(primaryStoreCard)
                            }
                        }
                        else -> {
                            listOfStoreCardFeatures.add(virtualTempCard)
                            listOfStoreCardFeatures.add(primaryStoreCard)
                        }
                    }
                }
                false -> {
                    when(primaryStoreCard){
                        is StoreCardFeatureType.StoreCardIsTemporaryFreeze -> {
                           when(primaryStoreCard.upShellMessage){
                               is StoreCardUpShellMessage.FreezeCard -> {
                                   listOfStoreCardFeatures.add(primaryStoreCard)
                                   listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardFreezeCardUpShellMessage(primaryStoreCard.storeCard))
                               }
                               else -> {
                                   if (virtualCard != null)
                                       listOfStoreCardFeatures.add(virtualTempCard)
                                   listOfStoreCardFeatures.add(primaryStoreCard)
                               }
                           }
                        }
                        is StoreCardFeatureType.ActivateVirtualTempCard -> {
                            when(primaryStoreCard.upShellMessage){
                                is StoreCardUpShellMessage.ActivateVirtualTempCard -> {
                                    listOfStoreCardFeatures.add(primaryStoreCard)
                                }
                                else ->{
                                    listOfStoreCardFeatures.add(primaryStoreCard)
                                    listOfStoreCardFeatures.add(StoreCardFeatureType.StoreCardActivateVirtualTempCardUpShellMessage(primaryStoreCard.storeCard))
                                }
                            }
                        }
                        else -> listOfStoreCardFeatures.add(primaryStoreCard)
                    }
                }
            }
        }

        return listOfStoreCardFeatures
    }

    override fun getVirtualTempCardNumber() = getVirtualCard()?.number ?: ""
    override fun getVirtualTempCardSequence(): Int  = getVirtualCard()?.sequence ?: -1

    override fun isUpshellCardForFreezeStoreCard(): Boolean {
        val isPrimaryCardBlockRequiredEnabled = AppConfigSingleton.virtualTempCard?.primaryCardBlockRequired ?: false
        val isBlockTypeEmpty = (getBlockType(0) ?: StoreCardBlockType.NONE) == StoreCardBlockType.NONE
        val isGenerateVirtualTempCardEnabled = !isGenerateVirtualCard()
        return isPrimaryCardBlockRequiredEnabled && isBlockTypeEmpty && isGenerateVirtualTempCardEnabled
    }

    override fun isUpshellCardForActivateVirtualCard(): Boolean {
        val isPrimaryCardBlockRequiredEnabled = AppConfigSingleton.virtualTempCard?.primaryCardBlockRequired ?: false
        val isBlockTypeTemporary = (getBlockType(0) ?: StoreCardBlockType.NONE) == StoreCardBlockType.TEMPORARY
        val isGenerateVirtualTempCardEnabled = isGenerateVirtualCard()
        return isPrimaryCardBlockRequiredEnabled && isBlockTypeTemporary && isGenerateVirtualTempCardEnabled
    }

    override fun calculateUpShellMessage(storeCard: StoreCard?): StoreCardUpShellMessage {
        return if (isUpshellCardForFreezeStoreCard()){
            StoreCardUpShellMessage.FreezeCard(storeCard = storeCard)
        }else if (isUpshellCardForActivateVirtualCard()){
            StoreCardUpShellMessage.ActivateVirtualTempCard(storeCard = storeCard)
        }else {
            StoreCardUpShellMessage.NoMessage
        }
    }

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
        return AppConfigSingleton.virtualTempCard?.primaryCardBlockRequired  == true && isActivateVirtualTempCard() && isBlockTypeTemporary(primaryCardIndex)
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

            isTemporaryFrozenStoreCardAndIsGenerateVirtualTempCardTrue(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsActivateVirtualTempCardAndIsFreezeCard(storeCard = storeCard, upShellMessage = calculateUpShellMessage(storeCard))

            isActivateVirtualTempCard() -> StoreCardFeatureType.ActivateVirtualTempCard(
                storeCard,
                isBlockTypeTemporary(primaryCardIndex), upShellMessage = calculateUpShellMessage(storeCard))


            isInstantCardReplacementJourneyEnabled(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsInstantReplacementCardAndInactive(
                storeCard
            )

            //Unfreeze my card
            isBlockTypeTemporary(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(
                storeCard,
                true,
                upShellMessage = calculateUpShellMessage(storeCard)
            )

            isUnFreezeTemporaryStoreCard(primaryCardIndex) -> StoreCardFeatureType.StoreCardIsTemporaryFreeze(
                storeCard,
                false,
                upShellMessage = calculateUpShellMessage(storeCard)
            )

            // Manage your card
            else -> StoreCardFeatureType.ManageMyCard(storeCard)
        }
    }
}