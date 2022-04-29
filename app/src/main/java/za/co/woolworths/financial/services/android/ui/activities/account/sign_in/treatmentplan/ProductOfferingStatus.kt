package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigShowTreatmentPlan
import za.co.woolworths.financial.services.android.util.Utils

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

    val accountOptions = AppConfigSingleton.accountOptions

    override fun minimumViewTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.showTreatmentPlanJourney)

    override fun minimumTakeUpTreatmentDelinquencyCycle(): Int? =
        getMinimumDelinquencyCycle(accountOptions?.collectionsStartNewPlanJourney)

    override fun isViewTreatmentPlanSupported(): Boolean =
        (getAccountsDelinquencyCycle() >= minimumViewTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(
            accountOptions?.showTreatmentPlanJourney
        )

    override fun isTakeUpTreatmentPlanJourneyEnabled(): Boolean {
        return (getAccountsDelinquencyCycle() >= minimumTakeUpTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(
            accountOptions?.collectionsStartNewPlanJourney
        )
    }

    override fun productGroupCode() = account?.productGroupCode?.lowercase()

    override fun getAccountsDelinquencyCycle(): Int = account?.delinquencyCycle ?: 999

    private fun isTreatmentPlanSupported(treatmentPlan: ConfigShowTreatmentPlan?): Boolean {
        val appBuildNumber = Utils.getAppBuildNumber()
        return when (productGroupCode()) {
            productGroupCodeSc -> appBuildNumber >= treatmentPlan?.storeCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            productGroupCodePl -> appBuildNumber >= treatmentPlan?.personalLoan?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            else -> appBuildNumber >= treatmentPlan?.creditCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
        }
    }

    private fun getMinimumDelinquencyCycle(treatmentPlan: ConfigShowTreatmentPlan?): Int? {
        return when (productGroupCode()) {
            productGroupCodeSc -> treatmentPlan?.storeCard?.minimumDelinquencyCycle
            productGroupCodePl -> treatmentPlan?.personalLoan?.minimumDelinquencyCycle
            else -> treatmentPlan?.creditCard?.minimumDelinquencyCycle
        }
    }

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
    fun isChargedOff():Boolean{
       return account?.productOfferingStatus.equals(
            Utils.ACCOUNT_CHARGED_OFF,
            ignoreCase = true
        )
    }
    fun isChargedOffCC() :Boolean{
        return productGroupCode() != productGroupCodeSc && productGroupCode() != productGroupCodePl && isChargedOff()
    }

    companion object {
        const val MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT = 999
        val productGroupCodeSc: String = AccountsProductGroupCode.STORE_CARD.groupCode.lowercase()
        val productGroupCodePl: String =
            AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase()
    }
}