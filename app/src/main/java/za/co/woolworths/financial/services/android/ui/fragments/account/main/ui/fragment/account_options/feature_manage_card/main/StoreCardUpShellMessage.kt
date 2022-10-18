package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard

sealed class StoreCardUpShellMessage : Parcelable {

    @Parcelize
    data class FreezeCard(var storeCard: StoreCard?) : StoreCardUpShellMessage()

    @Parcelize
    data class ActivateVirtualTempCard(var storeCard: StoreCard?) : StoreCardUpShellMessage()

    @Parcelize
    object NoMessage : StoreCardUpShellMessage()

}