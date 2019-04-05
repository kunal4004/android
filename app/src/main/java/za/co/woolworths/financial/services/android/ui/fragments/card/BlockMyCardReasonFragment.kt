package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
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

        btnBlockCard?.setOnClickListener { (activity as? AppCompatActivity)?.let { navigateToPermanentCardBlockFragment(it) } }
        blockCardRadioGroup?.setOnCheckedChangeListener { rad, id -> btnBlockCard?.isEnabled = rad.id != -1 }
    }

    fun processBlockCardRequest() {
        replaceFragment(
                fragment = ProcessBlockCardFragment.newInstance(),
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