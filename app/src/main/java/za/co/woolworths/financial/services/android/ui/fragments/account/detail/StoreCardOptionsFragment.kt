package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.util.*

class StoreCardOptionsFragment : AccountsOptionFragment() {

    private var accountStoreCardCallWasCompleted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_store_card)

        autoConnectListener()

        if (mCardPresenterImpl?.isDebitOrderActive() == VISIBLE) {
            debitOrderViewGroup?.visibility = VISIBLE
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, "#bad110")
        } else {
            debitOrderViewGroup?.visibility = GONE
        }
        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0
    }

    private fun autoConnectListener() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection && !accountStoreCardCallWasCompleted) {
                        navigateToGetStoreCard()
                    }
                }
            })
        }
    }

    private fun navigateToGetStoreCard() {
        Handler().postDelayed({
            navigateToGetStoreCards()
        }, 100)
    }

    override fun showOnStoreCardFailure(error: Throwable?) {
        activity?.let { ErrorHandlerView(it).showToast() }
    }


    override fun handleStoreCardCardsSuccess(storeCardResponse: StoreCardsResponse) {
        super.handleStoreCardCardsSuccess(storeCardResponse)
        hideStoreCardProgress()
        accountStoreCardCallWasCompleted = true

        when (storeCardResponse.httpCode) {
            200 -> {
                when (mCardPresenterImpl?.getStoreCardBlockType()) {
                    true -> {
                        cardDetailImageView?.setImageDrawable(bindDrawable(R.drawable.card_freeze))
                        manageMyCardTextView?.text = bindString(R.string.unfreeze_my_card_label)
                        tempFreezeTextView?.let{KotlinUtils.roundCornerDrawable(it, "#FF7000")}
                        tempFreezeTextView?.text = bindString(R.string.freeze_temp_label)
                        tempFreezeTextView?.visibility = VISIBLE
                        myCardDetailTextView?.visibility = GONE
                    }
                    else -> {
                        cardDetailImageView?.setImageDrawable(bindDrawable(R.drawable.w_store_card))
                        manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                        tempFreezeTextView?.visibility = GONE
                        myCardDetailTextView?.visibility = VISIBLE
                    }
                }
            }
            440 -> activity?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, storeCardResponse.response?.stsParams, it) }

            else -> {
                val desc = storeCardResponse.response?.desc ?: ""
                Utils.showGeneralErrorDialog(activity, desc)
            }
        }
    }

    override fun showUnBlockStoreCardCardDialog() {
        val storeCardResponse = mCardPresenterImpl?.getStoreCardResponse()
        val temporaryFreezeStoreCard = TemporaryFreezeStoreCard(storeCardResponse, object : ITemporaryCardFreeze {

            override fun onTemporaryCardUnFreezeConfirmed() {
                super.onTemporaryCardUnFreezeConfirmed()
                mCardPresenterImpl?.navigateToMyCardDetailActivity(true)
            }
        })

        temporaryFreezeStoreCard.showUnFreezeStoreCardDialog(childFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE) {
            navigateToGetStoreCard()
        }
    }
}