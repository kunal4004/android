package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.RewardVipExclusiveFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.wreward.unique_locators.WRewardUniqueLocatorsHelper
import za.co.woolworths.financial.services.android.util.Utils


class VIPExclusiveFragment : Fragment(R.layout.reward_vip_exclusive_fragment) {

    companion object {
        fun newInstance() = VIPExclusiveFragment()
    }

    private lateinit var binding: RewardVipExclusiveFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = RewardVipExclusiveFragmentBinding.bind(view)
        binding.apply {
            tvTermsCondition?.apply {
                activity?.apply {
                    paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    text =
                        WRewardBenefitActivity.convertWRewardCharacter(bindString(R.string.benefits_term_and_condition_link))
                    setOnClickListener {
                        Utils.openLinkInInternalWebView(AppConfigSingleton.wrewardsTCLink, true)
                    }
                }
            }
            WRewardUniqueLocatorsHelper.setVipExclusiveFragLocators(tvVipExclusiveTitle
            ,imVIPFlash
            ,tvVIPFlashDesc
            ,imWelcomeVoucher
            ,tvVIPFlash
            ,imWelcomeVoucher2
            ,tvBirthdayVoucher
            ,imWelcomeVoucher3
            ,tvBirthdayVoucher2
            ,tvGetToVIP
            ,tvCalculateStatusLevel
            ,tvCalculateStatusLevelPara1
            ,tvCalculateStatusLevelPara2
            ,tvCalculateStatusLevelPara3
            )
        }
    }
}