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
        var blockReason: Int? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.block_my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBlockCard?.setOnClickListener {
            (activity as? AppCompatActivity)?.let {
                navigateToPermanentCardBlockFragment(it)
            }
        }

        blockCardRadioGroup?.setOnCheckedChangeListener { radioGroup, index ->
            btnBlockCard?.isEnabled = radioGroup.id != -1
            when (radioGroup?.checkedRadioButtonId) {
                R.id.radDamaged -> blockReason = 1
                R.id.radLost -> blockReason = 2
                R.id.radStolen -> blockReason = 3
                R.id.radNotReceived -> blockReason = 4
            }
        }
    }

    override fun onResume() {
        super.onResume()
       showToolbar()
    }

    fun processBlockCardRequest() {
        replaceFragment(
                fragment = ProcessBlockCardFragment.newInstance(false, blockReason),
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