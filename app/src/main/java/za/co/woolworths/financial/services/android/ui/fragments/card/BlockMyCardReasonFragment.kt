package za.co.woolworths.financial.services.android.ui.fragments.card

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.block_my_card_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment


class BlockMyCardReasonFragment : MyCardExtension() {

    companion object {
        fun newInstance() = BlockMyCardReasonFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.block_my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        blockCardRadioGroup?.setOnCheckedChangeListener { rad, id -> btnBlockCard?.isEnabled = rad.id != -1 }

        btnBlockCard?.setOnClickListener {
            (activity as? AppCompatActivity)?.let {
                btnBlockCard?.text = ""
                blockUIProgressState(VISIBLE)
                Handler().postDelayed({
                    blockUIProgressState(GONE)
                    btnBlockCard?.text = activity?.resources?.getString(R.string.block_card_title)
                            ?: ""
                    navigateToPermanentCardBlockFragment(it)
                }, 2000)

            }
        }
    }

    private fun blockUIProgressState(state: Int) {
        pbBlockUI?.visibility = state
        pbBlockUI?.indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    fun processBlockCardRequest() {
        replaceFragment(
                fragment = ProcessBlockCardFragment.newInstance(false),
                tag = ProcessBlockCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }
}