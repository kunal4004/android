package za.co.woolworths.financial.services.android.ui.fragments.account.take_up_treatment_plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GetAPaymentPlanFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.setNavigationBarColor
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.updateStatusBarColor
import za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog.ViewTreatmentPlanDialogFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils

class GetAPaymentPlanFragment : Fragment() {

    private var mEligibilityPlan: EligibilityPlan? = null
    private lateinit var binding: GetAPaymentPlanFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GetAPaymentPlanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStatusBarColor(R.color.white, false)
        setNavigationBarColor(R.color.white)
        mEligibilityPlan = arguments?.getSerializable(ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN) as? EligibilityPlan

        when(mEligibilityPlan?.productGroupCode) {
            ProductGroupCode.SC -> {
                binding.treatmentPlanImageView.setImageResource(R.drawable.image_treatement_plan_sc)
            }
            ProductGroupCode.PL -> {
                binding.treatmentPlanImageView.setImageResource(R.drawable.image_treatement_plan_pl)
            }
            ProductGroupCode.CC -> {
                binding.treatmentPlanImageView.setImageResource(R.drawable.image_treatement_plan_cc)
            }
        }

        with(binding.viewPlanOptionsButton) {
            setOnClickListener {
                var collectionsUrl: String? = ""
                var exitUrl: String? = ""
                val accountOptions = AppConfigSingleton.accountOptions

                when (mEligibilityPlan?.productGroupCode) {
                    ProductGroupCode.SC -> {
                        collectionsUrl =accountOptions?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = accountOptions?.showTreatmentPlanJourney?.storeCard?.exitUrl
                    }

                    ProductGroupCode.PL -> {
                        collectionsUrl = accountOptions?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = accountOptions?.showTreatmentPlanJourney?.personalLoan?.exitUrl
                    }

                    ProductGroupCode.CC -> {
                        collectionsUrl = accountOptions?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl
                        exitUrl = accountOptions?.collectionsStartNewPlanJourney?.creditCard?.exitUrl
                    }
                }

                val url = collectionsUrl + mEligibilityPlan?.appGuid

                when (accountOptions?.collectionsStartNewPlanJourney?.renderMode) {
                    AvailableFundFragment.NATIVE_BROWSER ->
                        KotlinUtils.openUrlInPhoneBrowser(url, activity)

                    else ->
                        KotlinUtils.openLinkInInternalWebView(
                            activity,
                            url,
                            true,
                            exitUrl
                        )
                }
            }
        }
    }
}