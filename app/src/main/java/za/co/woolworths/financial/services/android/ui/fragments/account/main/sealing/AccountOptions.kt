package za.co.woolworths.financial.services.android.ui.fragments.account.main.sealing

import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType

sealed class AccountOptions {
    data class ViewTreatmentPlan(val isVisible: Boolean = false) : AccountOptions()
    data class SetUpAPaymentPlan(val isVisible: Boolean = false) : AccountOptions()
    data class PaymentOptions(val isVisible: Boolean = false) : AccountOptions()
    data class BalanceProtectionInsurance(val status : BpiInsuranceApplicationStatusType ,val leadGen: BpiInsuranceApplication? = null) : AccountOptions()
    data class WithdrawCashNow(val isVisible: Boolean = false) : AccountOptions()
    data class DebitOrder(val isActive: Boolean = false) : AccountOptions()
}
