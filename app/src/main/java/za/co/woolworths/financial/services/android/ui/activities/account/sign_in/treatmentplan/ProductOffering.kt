package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan

import android.util.Log
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.models.dto.TreatmentPlan
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.Utils

sealed class AccountOfferingState {
    object AccountInGoodStanding : AccountOfferingState() //when productOfferingGoodStanding == true
    object AccountIsInArrears: AccountOfferingState()//account is in arrears
    object AccountIsChargedOff : AccountOfferingState() //account is in arrears for more than 6 months
    object MakeGetEligibilityCall : AccountOfferingState()
}

interface IProductOffering {
    fun state(result: (AccountOfferingState) -> Unit)
    fun minimumViewTreatmentDelinquencyCycle(): Int?
    fun minimumTakeUpTreatmentDelinquencyCycle(): Int?
    fun isViewTreatmentPlanSupported(): Boolean?
    fun isTakeUpTreatmentPlanSupported(): Boolean
    fun productGroupCode(): String?
    fun getAccountsDelinquencyCycle() : Int
}

class ProductOffering(private val account: Account?) : IProductOffering {

    override fun minimumViewTreatmentDelinquencyCycle(): Int? = getMinimumDelinquencyCycle(WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney)

    override fun minimumTakeUpTreatmentDelinquencyCycle(): Int? = getMinimumDelinquencyCycle(WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney)

    override fun isViewTreatmentPlanSupported(): Boolean = (getAccountsDelinquencyCycle() >= minimumViewTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(WoolworthsApplication.getAccountOptions()?.showTreatmentPlanJourney)

    override fun isTakeUpTreatmentPlanSupported(): Boolean = (getAccountsDelinquencyCycle() >= minimumTakeUpTreatmentDelinquencyCycle() ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT) && isTreatmentPlanSupported(WoolworthsApplication.getAccountOptions()?.collectionsStartNewPlanJourney)

    override fun productGroupCode() = account?.productGroupCode?.lowercase()

    override fun getAccountsDelinquencyCycle(): Int = account?.delinquencyCycle ?: 999

    private fun isTreatmentPlanSupported(treatmentPlan: TreatmentPlan?): Boolean {
        val appBuildNumber = Utils.getAppBuildNumber()
        return when (productGroupCode()) {
            productGroupCodeSc -> appBuildNumber >= treatmentPlan?.personalLoan?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            productGroupCodePl -> appBuildNumber >= treatmentPlan?.storeCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
            else -> appBuildNumber >= treatmentPlan?.creditCard?.minimumSupportedAppBuildNumber ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT
        }
    }

    private fun getMinimumDelinquencyCycle(treatmentPlan: TreatmentPlan?): Int? {
        return when (productGroupCode()) {
            productGroupCodeSc -> treatmentPlan?.storeCard?.minimumDelinquencyCycle
            productGroupCodePl -> treatmentPlan?.personalLoan?.minimumDelinquencyCycle
            else -> treatmentPlan?.creditCard?.minimumDelinquencyCycle
        }
    }

    override fun state(result: (AccountOfferingState) -> Unit) {
        val isAccountProductOfferingGoodStanding = account?.productOfferingGoodStanding ?: false
        val isAccountChargeOff = account?.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)

        val accountOfferingState = when (isAccountProductOfferingGoodStanding) {
            true -> AccountOfferingState.AccountInGoodStanding
            false -> when (isAccountChargeOff) {
                true -> AccountOfferingState.AccountIsChargedOff
                false -> AccountOfferingState.MakeGetEligibilityCall
            }
        }

        result(accountOfferingState)
    }

    companion object {
        const val MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT = 999
        val productGroupCodeSc: String = AccountsProductGroupCode.STORE_CARD.groupCode.lowercase()
        val productGroupCodePl: String = AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase()
    }
}