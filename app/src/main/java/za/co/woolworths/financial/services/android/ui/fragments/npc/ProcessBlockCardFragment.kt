package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProcessBlockCardFragmentBinding
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities

class ProcessBlockCardFragment : MyCardExtension(R.layout.process_block_card_fragment), IProgressAnimationState {

    private lateinit var binding: ProcessBlockCardFragmentBinding
    private var mBlockCardReason: Int = 0
    private var mCardWasBlocked: Boolean = false
    private var mPostBlockMyCard: Call<BlockMyCardResponse>? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ProcessBlockCardFragmentBinding.bind(view)

        (activity as? AppCompatActivity)?.addFragment(
                fragment = ProgressStateFragment.newInstance(this),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )

        binding.incLinkCardSuccessFulView.okGotItButton?.setOnClickListener {
            (activity as? AppCompatActivity)?.let {
                it.setResult(MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE)
                it.finish()
            }
        }
        hideToolbarIcon()

        if (!mCardWasBlocked)
            executeBlockCard()

        binding.incNPCBlockCardFailure.btnRetry?.setOnClickListener {
            if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                progressState()?.restartSpinning()
                binding.incNPCBlockCardFailure?.root?.visibility = GONE
                binding.incProcessingTextLayout?.root?.visibility = VISIBLE
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
        binding.incLinkCardSuccessFulView?.root?.visibility = VISIBLE
        binding.incProcessingTextLayout?.root?.visibility = GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAnimationEnd(cardIsBlocked: Boolean) {
        when (mCardWasBlocked) {
            true -> displayUnblockCardSuccess()
            false -> binding.displayBlockedCardSuccess(cardIsBlocked)
        }
    }

    fun blockMyCardRequest(blockMyCardRequest: BlockCardRequestBody, productOfferingId: String?) {
        productOfferingId?.let {
            mPostBlockMyCard = OneAppService().postBlockMyCard(blockMyCardRequest, it)
            mPostBlockMyCard?.enqueue(CompletionHandler(object : IResponseListener<BlockMyCardResponse> {
                override fun onSuccess(blockMyCardResponse: BlockMyCardResponse?) {
                    blockCardSuccessResponse(blockMyCardResponse)
                }

                override fun onFailure(error: Throwable?) {
                    activity?.apply {
                        runOnUiThread {
                            blockMyCardFailure()
                        }
                    }
                }
            }, BlockMyCardResponse::class.java))
        }
    }

    fun blockCardSuccessResponse(blockMyCardResponse: BlockMyCardResponse?) {
        blockMyCardResponse?.apply {
            when (httpCode) {
                200 -> progressState()?.animateSuccessEnd(true)
                440 -> activity?.let { activity -> response?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, "", activity) } }
                else -> progressState()?.animateSuccessEnd(false)
            }
        }
    }

    fun blockMyCardFailure() {
        progressState()?.animateSuccessEnd(false)
        binding.displayBlockedCardSuccess(false)
    }

    private fun ProcessBlockCardFragmentBinding.displayBlockedCardSuccess(cardIsBlocked: Boolean) {
        if (cardIsBlocked) {
            incBlockCardSuccess?.root?.visibility = VISIBLE
            incProcessingTextLayout?.root?.visibility = GONE
            object : CountDownTimer(1500, 100) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    activity?.apply {
                        setResult(MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE,
                                Intent().putExtra(MyCardDetailActivity.REFRESH_MY_CARD_DETAILS, true))
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        finish()
                    }
                }
            }.start()
        } else {
            incNPCBlockCardFailure?.root?.visibility = VISIBLE
            incProcessingTextLayout?.root?.visibility = GONE
            (activity as? BlockMyCardActivity)?.iconVisibility(VISIBLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPostBlockMyCard?.apply {
            if (!isCanceled)
                cancel()
        }
        activity?.supportFragmentManager?.apply {
            if (findFragmentById(R.id.flProgressIndicator) != null) {
                findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
            }
        }
    }
}