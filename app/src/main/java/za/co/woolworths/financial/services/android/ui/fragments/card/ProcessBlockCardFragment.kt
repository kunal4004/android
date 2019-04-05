package za.co.woolworths.financial.services.android.ui.fragments.card

import android.app.Activity.RESULT_OK
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.process_block_card_fragment.*
import android.view.MenuInflater
import android.view.View.GONE
import android.view.View.VISIBLE


class ProcessBlockCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = ProcessBlockCardFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.process_block_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO:: TO BE REMOVED, USED ONLY FOR PROTOTYPE DEMONSTRATION
        activity?.apply {
            pbProcessRequest?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.SRC_IN)

            val handler = Handler()
            handler.postDelayed({
                imSuccessBlock?.visibility = VISIBLE
                pbProcessRequest?.visibility = GONE
                tvProcessingYourRequestDuration?.visibility = GONE
                tvProcessBlockCardRequestStatus?.text = getString(R.string.card_block_success)
                val successHandler = Handler()
                successHandler.postDelayed({
                    setResult(RESULT_OK)
                    finish()
                    overridePendingTransition(0, 0)
                }, 1000)

            }, 2000)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }
}