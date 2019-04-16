package za.co.woolworths.financial.services.android.ui.fragments.card

import android.app.Activity.RESULT_OK
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
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class ProcessBlockCardFragment : MyCardExtension() {

    private var mCardWasBlocked: Boolean? = null

    companion object {
        const val NPC_CARD_LINKED_SUCCESS_RESULT_CODE = 3021
        private const val CARD_WAS_BLOCKED = "CARD_WAS_BLOCKED"
        fun newInstance(cardBlocked: Boolean) = ProcessBlockCardFragment().withArgs {
            putBoolean(CARD_WAS_BLOCKED, cardBlocked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            mCardWasBlocked = getBoolean(CARD_WAS_BLOCKED, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.process_block_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO:: TO BE REMOVED, USED ONLY FOR PROTOTYPE DEMONSTRATION
        activity?.apply {
            val handler = Handler()
            handler.postDelayed({
                when (mCardWasBlocked) {
                    true -> displayUnblockCardSuccess()
                    false -> displayBlockedCardSuccess()
                }
            }, 1500)
        }

        btn_ok_got_it?.setOnClickListener { navigateToMyCardActivity() }

        hideToolbarIcon()
    }

    private fun hideToolbarIcon() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
        }
    }

    private fun navigateToMyCardActivity() {
        activity?.apply {
            setResult(NPC_CARD_LINKED_SUCCESS_RESULT_CODE, null)
            finish()
            overridePendingTransition(0, 0)
        }
    }

    private fun displayUnblockCardSuccess() {
        successProgressAnimation()
        incLinkCardSuccessFulView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    private fun displayBlockedCardSuccess() {
        val successHandler = Handler()
        successHandler.postDelayed({
            activity.apply {
                setResult(RESULT_OK)
                finish()
                overridePendingTransition(0, 0)
            }
        }, 800)

        successProgressAnimation()
        incBlockCardSuccess?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    private fun successProgressAnimation() {
        imSuccessAnimation?.visibility = VISIBLE
        pbProcessRequest?.visibility = GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }
}