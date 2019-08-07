package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.reward_benefit_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity

class RewardBenefitFragment : Fragment() {

    companion object {
        private const val REWARD_URL = "https://www.woolworths.co.za/corporate/cmp205288"
        fun newInstance() = RewardBenefitFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reward_benefit_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTermsCondition?.apply {
            text = (activity as? WRewardBenefitActivity)?.convertWRewardCharacter(getString(R.string.benefits_term_and_condition_link))
            movementMethod = LinkMovementMethod.getInstance()
            tvTermsCondition?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                with(Intent(Intent.ACTION_VIEW)) {
                    data = Uri.parse(REWARD_URL)
                    startActivity(this)
                }
            }
        }
    }
}