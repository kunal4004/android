package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse

interface ITemporaryCardFreeze {
    fun showProgress() {}
    fun onTemporaryCardFreezeConfirmed() {}
    fun onTemporaryCardFreezeCanceled() {}
    fun onTemporaryCardUnFreezeConfirmed() {}
    fun onTemporaryCardUnFreezeCanceled() {}
    fun onFreezeCardSuccess(response: BlockMyCardResponse?) {}
    fun onStoreCardFailure(error: Throwable?) {}
    fun onUnFreezeSuccess(response: UnblockStoreCardResponse?)
}