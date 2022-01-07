package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.reward_vip_exclusive_fragment.tvTermsCondition
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils


class VIPExclusiveFragment : Fragment() {

    companion object {
        fun newInstance() = VIPExclusiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.reward_vip_exclusive_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        tvTermsCondition?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvTermsCondition?.apply {
            activity?.apply {
                text = WRewardBenefitActivity.convertWRewardCharacter(bindString(R.string.benefits_term_and_condition_link))
                setOnClickListener {
                    Utils.openLinkInInternalWebView(AppConfigSingleton.wrewardsTCLink)
                }
            }
        }
    }
}