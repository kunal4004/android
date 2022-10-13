package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigShowTreatmentPlan
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOfferingState
import za.co.woolworths.financial.services.android.util.Utils



interface IProductOffering {
    fun state(result: (AccountOfferingState) -> Unit)
    fun minimumViewTreatmentDelinquencyCycle(): Int?
    fun minimumTakeUpTreatmentDelinquencyCycle(): Int?
    fun isViewTreatmentPlanSupported(): Boolean?
    fun isTakeUpTreatmentPlanJourneyEnabled(): Boolean
    fun productGroupCode(): String?
    fun getAccountsDelinquencyCycle(): Int
}

class ProductOfferingStatus(private val account: Account?) : IProductOffering {

    // consumed
    val accountOptions = AppConfigSingleton.accountOptions

    override fun isViewTreatmentPlanSupported(): Boolean =
        (getAccountsDelinquencyCycle() >= minimumViewTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(
            accountOptions?.showTreatmentPlanJourney
        )

    // consumed
    override fun isTakeUpTreatmentPlanJourneyEnabled(): Boolean {
        return (getAccountsDelinquencyCycle() >= minimumTakeUpTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(
            accountOptions?.collectionsStartNewPlanJourney
        )
    }

    //consumed
    override fun productGroupCode() = account?.productGroupCode?.lowercase()

    // consumed
    override fun getAccountsDelinquencyCycle(): Int = account?.delinquencyCycle ?: 999

    //consumed
    private fun isTreatmentPlanSupported(treatmentPlan: ConfigShowTreatmentPlan?): Boolean {
        val appBuildNumber = Utils.getAppBuildNumber()
        return when (productGroupCode()) {
            productGroupCodeSc -> appBuildNumber >= treatmentPlan?.storeCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            productGroupCodePl -> appBuildNumber >= treatmentPlan?.personalLoan?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            else -> appBuildNumber >= treatmentPlan?.creditCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
        }
    }

    //consumed
    private fun getMinimumDelinquencyCycle(treatmentPlan: ConfigShowTreatmentPlan?): Int? {
        return when (productGroupCode()) {
            productGroupCodeSc -> treatmentPlan?.storeCard?.minimumDelinquencyCycle
            productGroupCodePl -> treatmentPlan?.personalLoan?.minimumDelinquencyCycle
            else -> treatmentPlan?.creditCard?.minimumDelinquencyCycle
        }
    }

    // consumed
    override fun state(result: (AccountOfferingState) -> Unit) {
        val accountOfferingState = when (account?.productOfferingGoodStanding ?: false) {
            true -> AccountOfferingState.AccountInGoodStanding
            false -> {
                when {
                    isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                    isViewTreatmentPlanSupported() -> if (isChargedOff()) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isChargedOff()) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            }
        }
        result(accountOfferingState)
    }

    override fun minimumViewTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.showTreatmentPlanJourney)


    override fun minimumTakeUpTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.collectionsStartNewPlanJourney)


    fun isChargedOff(): Boolean {
        return account?.productOfferingStatus.equals(
            Utils.ACCOUNT_CHARGED_OFF,
            ignoreCase = true
        )
    }

    fun isChargedOffCC(): Boolean {
        return productGroupCode() != productGroupCodeSc && productGroupCode() != productGroupCodePl && isChargedOff()
    }

    // consumed
    companion object {
        const val MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT = 999
        val productGroupCodeSc: String = AccountsProductGroupCode.STORE_CARD.groupCode.lowercase()
        val productGroupCodePl: String =
            AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase()
    }
}