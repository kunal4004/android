package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.util.KotlinUtils

class  StoreCardEnhancementConstant() {
    companion object {
        const val NewCard = "NewCard"
    }
}

sealed class StoreCardFeatureType : Parcelable {

    @Parcelize
    data class StoreCardFreezeCardUpShellMessage(
        var storeCard: StoreCard?) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardActivateVirtualTempCardUpShellMessage(
        var storeCard: StoreCard?) : StoreCardFeatureType()


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
        var upShellMessage : StoreCardUpShellMessage,
        var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname(),
    ) : StoreCardFeatureType()

    @Parcelize
    data class StoreCardIsActivateVirtualTempCardAndIsFreezeCard(
        var storeCard: StoreCard?,
        var isStoreCardFrozen: Boolean = true,
        var isAnimationEnabled: Boolean = false,
        var upShellMessage : StoreCardUpShellMessage,
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
        var upShellMessage : StoreCardUpShellMessage,
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
    data class ManageMyCard(var storeCard: StoreCard?,
                            var cardHolderName: String? = KotlinUtils.getCardHolderNameSurname()) : StoreCardFeatureType()
}
