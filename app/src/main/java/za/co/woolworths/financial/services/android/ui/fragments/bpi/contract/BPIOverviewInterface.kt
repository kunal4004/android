package za.co.woolworths.financial.services.android.ui.fragments.bpi.contract

import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverviewFromConfig
import za.co.woolworths.financial.services.android.models.dto.InsuranceType

interface BPIOverviewInterface {
    fun isCovered(): Boolean
    fun coveredUncoveredList(): MutableList<BalanceProtectionInsuranceOverviewFromConfig>
    fun getInsuranceType(): MutableList<InsuranceType>
    fun coveredList(): MutableList<BalanceProtectionInsuranceOverviewFromConfig>
    fun effectiveDate(effectiveDate : String?): String
}