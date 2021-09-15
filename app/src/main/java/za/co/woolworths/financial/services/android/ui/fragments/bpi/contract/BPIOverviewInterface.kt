package za.co.woolworths.financial.services.android.ui.fragments.bpi.contract

import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType

interface BPIOverviewInterface {
    fun isCovered(): Boolean
    fun coveredUncoveredList(): MutableList<BalanceProtectionInsuranceOverview>
    fun getInsuranceType(): MutableList<InsuranceType>
    fun coveredList(): MutableList<BalanceProtectionInsuranceOverview>
    fun effectiveDate(effectiveDate : String?): String
    fun getAccount(): Account?
    fun navigateToOverviewDetail(): Pair<BalanceProtectionInsuranceOverview, Boolean>
}