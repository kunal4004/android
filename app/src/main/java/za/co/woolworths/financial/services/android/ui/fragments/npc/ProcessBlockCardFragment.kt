package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.npc_block_card_failure.*
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.REQUEST_CODE_BLOCK_MY_STORE_CARD
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.PERMANENT
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.PersistenceLayer
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION

class ProcessBlockCardFragment : BlockMyCardRequestExtension(), IProgressAnimationState {

    private var mBlockCardReason: Int = 0
    private var mCardWasBlocked: Boolean = false

    companion object {
        const val CARD_BLOCKED = "CARD_BLOCKED"
        const val BLOCK_CARD_REASON = "BLOCK_CARD_REASON"
        const val RESULT_CODE_BLOCK_CODE_SUCCESS = 556
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )

        okGotItButton?.setOnClickListener { navigateToMyCardActivity(false) }
        hideToolbarIcon()

        if (!mCardWasBlocked)
            executeBlockCard()

        btnRetry?.setOnClickListener {
            if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                progressState()?.restartSpinning()
                incNPCBlockCardFailure?.visibility = GONE
                incProcessingTextLayout?.visibility = VISIBLE
                (activity as? BlockMyCardActivity)?.iconVisibility(GONE)
                executeBlockCard()
            }
        }

    }

    private fun executeBlockCard() {
        val storeCard = (activity as? BlockMyCardActivity)?.getAccountStoreCards()
        val storeCardDetail = (activity as? BlockMyCardActivity)?.getStoreCardDetail()?.storeCardsData
        (activity as? BlockMyCardActivity)?.getAccountStoreCards()?.apply {
            storeCardDetail?.productOfferingId?.let {
                blockMyCardRequest(BlockCardRequestBody(storeCardDetail.visionAccountNumber, storeCard?.number
                        ?: "", storeCard?.sequence?.toInt() ?: 0, mBlockCardReason), it)
            }
        }
    }


    private fun displayUnblockCardSuccess() {
        incLinkCardSuccessFulView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAnimationEnd(cardIsBlocked: Boolean) {
        when (mCardWasBlocked) {
            true -> displayUnblockCardSuccess()
            false -> displayBlockedCardSuccess(cardIsBlocked)
        }
    }

    override fun blockCardSuccessResponse(blockMyCardResponse: BlockMyCardResponse?) {
        blockMyCardResponse?.apply {
            when (httpCode) {
                200 -> progressState()?.animateSuccessEnd(true)
                440 -> activity?.let { activity -> response?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, "", activity) } }
                else -> progressState()?.animateSuccessEnd(false)
            }
        }
    }

    override fun blockMyCardFailure() {
        progressState()?.animateSuccessEnd(false)
        displayBlockedCardSuccess(false)
    }

    private fun displayBlockedCardSuccess(cardIsBlocked: Boolean) {
        if (cardIsBlocked) {
            incBlockCardSuccess?.visibility = VISIBLE
            incProcessingTextLayout?.visibility = GONE
            object : CountDownTimer(1500, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    activity?.apply {
                        val storeCard = (this as? BlockMyCardActivity)?.getStoreCardDetail()
                        storeCard?.storeCardsData?.primaryCards?.get(PRIMARY_CARD_POSITION)?.blockType = PERMANENT
                        PersistenceLayer.getInstance().executeDeleteQuery("DELETE FROM ApiRequest WHERE endpoint LIKE '%user/accounts'")
                        val displayStoreCardDetail = Intent(this, MyCardDetailActivity::class.java)
                        displayStoreCardDetail.putExtra(STORE_CARD_DETAIL, Utils.objectToJson(storeCard))
                        startActivityForResult(displayStoreCardDetail, REQUEST_CODE_BLOCK_MY_STORE_CARD)
                        setResult(RESULT_CODE_BLOCK_CODE_SUCCESS)
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        finish()
                    }
                }
            }.start()
        } else {
            incNPCBlockCardFailure?.visibility = VISIBLE
            incProcessingTextLayout?.visibility = GONE
            (activity as? BlockMyCardActivity)?.iconVisibility(VISIBLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.supportFragmentManager?.apply {
            if (findFragmentById(R.id.flProgressIndicator) != null) {
                findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
            }
        }
    }
}