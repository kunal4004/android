package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigShowTreatmentPlan
import za.co.woolworths.financial.services.android.util.Utils

// consumed
sealed class AccountOfferingState {
    object AccountInGoodStanding : AccountOfferingState() //when productOfferingGoodStanding == true
    object AccountIsInArrears : AccountOfferingState()//account is in arrears
    object AccountIsChargedOff :
        AccountOfferingState() //account is in arrears for more than 6 months

    object MakeGetEligibilityCall : AccountOfferingState()
    object ShowViewTreatmentPlanPopupFromConfigForChargedOff : AccountOfferingState()
    object ShowViewTreatmentPlanPopupInArrearsFromConfig : AccountOfferingState()
}

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

    // consumed
    override fun minimumViewTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.showTreatmentPlanJourney)

    // consumed
    override fun minimumTakeUpTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.collectionsStartNewPlanJourney)

    // consumed
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
            productGroupCodeSc -> appBuildNumber >= treatmentPlan?.personalLoan?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            productGroupCodePl -> appBuildNumber >= treatmentPlan?.storeCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
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
                val isProductChargedOff = account?.productOfferingStatus.equals(
                    Utils.ACCOUNT_CHARGED_OFF,
                    ignoreCase = true
                )
                when {
                    !isProductChargedOff && isTakeUpTreatmentPlanJourneyEnabled() -> AccountOfferingState.MakeGetEligibilityCall
                    isViewTreatmentPlanSupported() -> if (isProductChargedOff) AccountOfferingState.ShowViewTreatmentPlanPopupFromConfigForChargedOff else AccountOfferingState.ShowViewTreatmentPlanPopupInArrearsFromConfig
                    else -> if (isProductChargedOff) AccountOfferingState.AccountIsChargedOff else AccountOfferingState.AccountIsInArrears
                }
            }
        }
        result(accountOfferingState)
    }

// consumed
    companion object {
        const val MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT = 999
        val productGroupCodeSc: String = AccountsProductGroupCode.STORE_CARD.groupCode.lowercase()
        val productGroupCodePl: String =
            AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase()
    }
}