package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.reward_vip_exclusive_fragment.tvTermsCondition
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import android.content.Intent
import android.net.Uri


class VIPExclusiveFragment : Fragment() {

    companion object {
        private const val REWARD_URL = "https://www.woolworths.co.za/corporate/cmp205288"
        fun newInstance() = VIPExclusiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reward_vip_exclusive_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        tvTermsCondition?.apply {
            text = WRewardBenefitActivity.convertWRewardCharacter(getString(R.string.benefits_term_and_condition_link))
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