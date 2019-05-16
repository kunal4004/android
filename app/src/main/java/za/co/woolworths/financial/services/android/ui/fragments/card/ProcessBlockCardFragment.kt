package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_card_linked_successful_layout.*
import kotlinx.android.synthetic.main.process_block_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import android.os.CountDownTimer
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class ProcessBlockCardFragment : ConfirmBlockCardRequestExtension(), IProgressAnimationState {

    private var mBlockCardReason: Int = 0
    private var mCardWasBlocked: Boolean? = null

    companion object {
        const val CARD_BLOCKED = "CARD_BLOCKED"
        const val BLOCK_CARD_REASON = "BLOCK_CARD_REASON"
        fun newInstance(cardBlocked: Boolean, blockReason: Int?) = ProcessBlockCardFragment().withArgs {
            putBoolean(CARD_BLOCKED, cardBlocked)
            putInt(BLOCK_CARD_REASON, blockReason ?: 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mCardWasBlocked = getBoolean(CARD_BLOCKED, false)
            mBlockCardReason = getInt(BLOCK_CARD_REASON)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.process_block_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )

        btn_ok_got_it?.setOnClickListener { navigateToMyCardActivity(false) }
        hideToolbarIcon()
        if (mCardWasBlocked == true) {
            // TODO UNBLOCK STORE CARD
        } else {
            val account = (activity as? BlockMyCardActivity)?.getStoreCardDetail()
            (activity as? BlockMyCardActivity)?.getCardDetail()?.apply {
                account?.productOfferingId?.let { blockMyCardRequest(BlockCardRequestBody(cardNumber, cardNumber, sequenceNumber.toString(), mBlockCardReason.toString()), it.toString()) }
            }
        }
    }

    private fun hideToolbarIcon() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    private fun displayUnblockCardSuccess() {
        incLinkCardSuccessFulView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    private fun displayBlockedCardSuccess() {
        incBlockCardSuccess?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
        object : CountDownTimer(1500, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
            }
        }.start()

        navigateToMyCardActivity(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    private fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAnimationEnd() {
        when (mCardWasBlocked) {
            true -> displayUnblockCardSuccess()
            false -> displayBlockedCardSuccess()
        }
    }

    override fun onSuccess(blockMyCardResponse: BlockMyCardResponse?) {
        blockMyCardResponse?.apply {
            when (httpCode) {
                200 -> progressState()?.animateSuccessEnd()
                440 -> activity?.let { activity -> response?.stsParams?.let { stsParams -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, stsParams, activity) } }
                else -> response?.desc?.let { Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR, it) }

            }
        }
    }

    override fun progressBarVisibility(visible: Boolean) {
    }
}