package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import android.view.MenuInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.npc_card_linked_successful_layout.*
import kotlinx.android.synthetic.main.process_block_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProcessBlockCardFragment : MyCardExtension() {
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
                fragment = ProgressStateFragment.newInstance(),
                tag = ProgressStateFragment::class.java.simpleName,
                containerViewId = R.id.flProgressIndicator
        )

        //TODO:: TO BE REMOVED, USED ONLY FOR PROTOTYPE DEMONSTRATION
        val handler = Handler()
        handler.postDelayed({
            when (mCardWasBlocked) {
                true -> displayUnblockCardSuccess()
                false -> displayBlockedCardSuccess()
            }
        }, 800)

        btn_ok_got_it?.setOnClickListener { navigateToMyCardActivity(false) }
        hideToolbarIcon()

        val successHandler = Handler()
        successHandler.postDelayed({
            progressState()?.animationStart()
        }, 100)
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
        val successHandler = Handler()
        successHandler.postDelayed({
            //            navigateToMyCardActivity(true)
            progressState()?.animateSuccessEnd()
        }, 800)

        incBlockCardSuccess?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }


    private fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

}