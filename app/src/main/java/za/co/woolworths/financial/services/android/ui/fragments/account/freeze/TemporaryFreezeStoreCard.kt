package za.co.woolworths.financial.services.android.ui.fragments.account.freeze

import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.UnblockStoreCardResponse
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryFreezeCardFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.TemporaryUnFreezeCardFragment
import za.co.woolworths.financial.services.android.util.StoreCardAPIRequest

open class TemporaryFreezeStoreCard(private val storeCardResponse: StoreCardsResponse?, private val temporaryCardFreezeInterface: ITemporaryCardFreeze?) {

    companion object {
        const val TEMPORARY = "temporary"
        const val PERMANENT = "permanent"
        const val NOW = ""
        const val BLOCK_REASON = 6
    }

    private fun primaryCard() = this.storeCardResponse?.storeCardsData?.primaryCards?.get(0)

    private fun storeCardsData() = this.storeCardResponse?.storeCardsData

    private fun blockType() = primaryCard()?.blockType

    fun freezeStoreCardDialog(childFragmentManager: FragmentManager?) {
        val temporaryFreezeCardFragment = TemporaryFreezeCardFragment(temporaryCardFreezeInterface)
        childFragmentManager?.let { cfm -> temporaryFreezeCardFragment.show(cfm, TemporaryFreezeCardFragment::class.java.simpleName) }
    }

    fun unFreezeStoreCardDialog(childFragmentManager: FragmentManager?) {
        val temporaryUnFreezeCardFragment =
                TemporaryUnFreezeCardFragment(temporaryCardFreezeInterface)
        childFragmentManager?.let { cfm -> temporaryUnFreezeCardFragment.show(cfm, TemporaryUnFreezeCardFragment::class.java.simpleName) }
    }

    fun showActiveTemporaryFreezeCard(switch: Switch?, cardFreezeImageView: ImageView?, cardStatusTextView: TextView?) {

        val isFreezeCardChecked = when (blockType()) {
            TEMPORARY -> true
            else -> false
        }

        switch?.isChecked = isFreezeCardChecked

        when (isFreezeCardChecked) {
            true -> {
                cardStatusTextView?.text = bindString(R.string.temp_freeze_label)
                cardFreezeImageView?.setImageDrawable(bindDrawable(R.drawable.card_freeze))
            }
            false -> {
                cardStatusTextView?.text = bindString(R.string.active)
                cardFreezeImageView?.setImageDrawable(bindDrawable(R.drawable.w_store_card))
            }
        }
    }

    fun blockStoreCardRequest() {
        val storeCardData = storeCardsData()
        val primaryCard = primaryCard()

        val productOfferingId = storeCardData?.productOfferingId
        val visionAccountNumber = storeCardData?.visionAccountNumber ?: ""
        val storeCardNumber = primaryCard?.number ?: ""
        val sequenceNumber = primaryCard?.sequence?.toInt() ?: 0

        val blockStoreCardRequestBody = BlockCardRequestBody(visionAccountNumber, storeCardNumber, sequenceNumber, BLOCK_REASON)

        temporaryCardFreezeInterface?.showProgress()
        StoreCardAPIRequest().blockCard(productOfferingId
                ?: "", blockStoreCardRequestBody, object : IResponseListener<BlockMyCardResponse> {
            override fun onSuccess(response: BlockMyCardResponse?) {
                temporaryCardFreezeInterface?.onFreezeCardSuccess(response)
            }

            override fun onFailure(error: Throwable?) {
                temporaryCardFreezeInterface?.onStoreCardFailure(error)
            }
        })
    }


    fun unblockStoreCardRequest() {

        val storeCardData = storeCardsData()
        val primaryCard = primaryCard()

        val productOfferingId = storeCardData?.productOfferingId
        val visionAccountNumber = storeCardData?.visionAccountNumber ?: ""
        val storeCardNumber = primaryCard?.number ?: ""
        val sequenceNumber = primaryCard?.sequence?.toInt() ?: 0

        val unblockStoreCardRequestBody = UnblockStoreCardRequestBody(visionAccountNumber, storeCardNumber, sequenceNumber.toString())

        temporaryCardFreezeInterface?.showProgress()

        StoreCardAPIRequest().unblockCard(productOfferingId
                ?: NOW, unblockStoreCardRequestBody, object : IResponseListener<UnblockStoreCardResponse> {
            override fun onSuccess(response: UnblockStoreCardResponse?) {
                temporaryCardFreezeInterface?.onUnFreezeSuccess(response)
            }

            override fun onFailure(error: Throwable?) {
                temporaryCardFreezeInterface?.onStoreCardFailure(error)
            }
        })
    }

}