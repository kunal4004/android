package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.graphics.Paint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RewardBenefitFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class RewardBenefitFragment :
    BaseFragmentBinding<RewardBenefitFragmentBinding>(RewardBenefitFragmentBinding::inflate) {

    companion object {
        fun newInstance() = RewardBenefitFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTermsCondition?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.tvTermsCondition?.apply {
            activity?.apply {
                text =
                    WRewardBenefitActivity.convertWRewardCharacter(getString(R.string.benefits_term_and_condition_link))
                movementMethod = LinkMovementMethod.getInstance()
                setOnClickListener {
                    Utils.openLinkInInternalWebView(AppConfigSingleton.wrewardsTCLink, true)
                }
            }
        }
        binding.apply {
            WRewardUniqueLocatorsHelper.setBenefitsFragLocators(
                tvTitle,
                tvBenefitSubTitle1,
                tvBenefitSubDesc1,
                tvBenefitSubTitle2,
                tvBenefitSubDesc2,
                tvBenefitSubTitle3,
                tvBenefitSubDesc3,
                tvTermsCondition
            )
        }
    }
}