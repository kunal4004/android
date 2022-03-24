package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main

import android.view.View.GONE
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.ProductGroupCode
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import javax.inject.Inject

interface IEligibilityImpl {
    fun getPopUpData(eligibilityPlan: EligibilityPlan?): DialogData
}

class EligibilityImpl @Inject constructor(
) : IEligibilityImpl {
    override fun getPopUpData(eligibilityPlan: EligibilityPlan?): DialogData {
        eligibilityPlan?.apply {
            when (productGroupCode?.value?.equals(eligibilityPlan.productGroupCode?.value)) {
                true -> {
                    when (actionText) {
                        ActionText.TAKE_UP_TREATMENT_PLAN.value -> {
                            return DialogData.VipDialog()
                        }
                        ActionText.START_NEW_ELITE_PLAN.value -> {
                            val eliteDialog = DialogData.EliteDialog()
                            if (productGroupCode == ProductGroupCode.CC) {
                                eliteDialog.firstButtonTitle = R.string.get_help_repayment
                                eliteDialog.secondButtonVisibility = GONE
                            }
                            return eliteDialog
                        }
                        ActionText.VIEW_TREATMENT_PLAN.value -> {
                            return DialogData.ViewPlanDialog()
                        }
                        ActionText.VIEW_ELITE_PLAN.value -> {
                            val viewPlanDialog = DialogData.ViewPlanDialog()
                            if (productGroupCode == ProductGroupCode.CC) {
                                viewPlanDialog.firstButtonTitle = R.string.view_your_payment_plan
                                viewPlanDialog.secondButtonVisibility = GONE
                            }
                            return viewPlanDialog
                        }
                    }
                }
            }
        }
        return DialogData.AccountInArrDialog()
    }
}