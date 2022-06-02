package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType

sealed class AccountOptionsScreenUI {
    data class ViewTreatmentPlan(
        val isVisible: Boolean = false,
        var eligibilityPlan: EligibilityPlan? = null
    ) : AccountOptionsScreenUI()

    data class SetUpAPaymentPlan(
        val isVisible: Boolean = false,
        var eligibilityPlan: EligibilityPlan? = null
    ) : AccountOptionsScreenUI()

    data class PaymentOptionsScreenUI(val isVisible: Boolean = false) : AccountOptionsScreenUI()
    data class BalanceProtectionInsurance(
        val status: BpiInsuranceApplicationStatusType,
        val leadGen: BpiInsuranceApplication? = null
    ) : AccountOptionsScreenUI()

    data class WithdrawCashNow(val isVisible: Boolean = false) : AccountOptionsScreenUI()
    data class DebitOrder(val isActive: Boolean = false) : AccountOptionsScreenUI()
}
