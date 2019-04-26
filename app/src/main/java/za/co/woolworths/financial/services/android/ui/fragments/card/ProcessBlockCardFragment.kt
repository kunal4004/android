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


class ProcessBlockCardFragment : MyCardExtension(), IProgressAnimationState {

    private var mCardWasBlocked: Boolean? = null

    companion object {
        const val CARD_BLOCKED = "CARD_BLOCKED"
        fun newInstance(cardBlocked: Boolean) = ProcessBlockCardFragment().withArgs {
            putBoolean(CARD_BLOCKED, cardBlocked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mCardWasBlocked = getBoolean(CARD_BLOCKED, false)
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

        //TODO:: TO BE REMOVED, USED ONLY FOR PROTOTYPE DEMONSTRATION
        btn_ok_got_it?.setOnClickListener { navigateToMyCardActivity(false) }
        hideToolbarIcon()
        val waitingTime = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                progressState()?.animateSuccessEnd()
            }
        }
        waitingTime.start()
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
        val successTick = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                navigateToMyCardActivity(true)
                incBlockCardSuccess?.visibility = VISIBLE
                incProcessingTextLayout?.visibility = GONE
            }
        }

        successTick.start()
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
}