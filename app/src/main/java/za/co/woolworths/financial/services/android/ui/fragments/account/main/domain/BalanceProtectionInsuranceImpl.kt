package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplication
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import javax.inject.Inject

interface IBalanceProtectionInsurance {
    fun getBalanceProtectionInsuranceLead(): BpiInsuranceApplication?
    fun isInsuranceCovered(): Boolean
    fun balanceProtectionInsuranceTag(): AccountOptionsScreenUI.BalanceProtectionInsurance
}

class BalanceProtectionInsuranceImpl @Inject constructor(private val product: AccountProductLandingDao?) :
    IBalanceProtectionInsurance {

    override fun getBalanceProtectionInsuranceLead(): BpiInsuranceApplication? {
        return product?.getAccountProduct()?.bpiInsuranceApplication
    }

    override fun isInsuranceCovered(): Boolean {
        return product?.getAccountProduct()?.insuranceCovered ?: false
    }

    override fun balanceProtectionInsuranceTag(): AccountOptionsScreenUI.BalanceProtectionInsurance {
        val insuranceLeadGen = getBalanceProtectionInsuranceLead()
        val isInsuranceCovered = isInsuranceCovered()
        var leadGen: BpiInsuranceApplication? = null
        val status = when {
            insuranceLeadGen != null -> when (insuranceLeadGen.status) {
                BpiInsuranceApplicationStatusType.COVERED,
                BpiInsuranceApplicationStatusType.NOT_OPTED_IN,
                BpiInsuranceApplicationStatusType.OPTED_IN -> {
                    leadGen = insuranceLeadGen
                    insuranceLeadGen.status
                }
                else -> BpiInsuranceApplicationStatusType.NOT_COVERED
            }
            else -> {
                leadGen = null
                when (isInsuranceCovered) {
                    true -> BpiInsuranceApplicationStatusType.INSURANCE_COVERED
                    false -> BpiInsuranceApplicationStatusType.NOT_COVERED
                }
            }
        }
        return AccountOptionsScreenUI.BalanceProtectionInsurance(status, leadGen)
    }
}