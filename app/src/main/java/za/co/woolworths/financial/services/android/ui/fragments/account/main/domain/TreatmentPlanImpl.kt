package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigAccountOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.ConfigShowTreatmentPlan
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface ITreatmentPlan {
    val product: Account?
    val configAccountOptions: ConfigAccountOptions?
    fun minimumViewTreatmentDelinquencyCycle(): Int?
    fun minimumTakeUpTreatmentDelinquencyCycle(): Int?
    fun isViewTreatmentPlanSupported(): Boolean
    fun isTakeUpTreatmentPlanJourneyEnabled(): Boolean
    fun productGroupCode(): String?
    fun getAccountsDelinquencyCycle(): Int?
}

class TreatmentPlanImpl @Inject constructor(private val accountProductLandingDao: AccountProductLandingDao) :
    ITreatmentPlan, IAccountProductLandingDao by accountProductLandingDao {

    override val configAccountOptions: ConfigAccountOptions?
        get() = AppConfigSingleton.accountOptions

    override fun minimumViewTreatmentDelinquencyCycle(): Int? {
        return getMinimumDelinquencyCycle(configAccountOptions?.showTreatmentPlanJourney)
    }

    override fun minimumTakeUpTreatmentDelinquencyCycle(): Int? {
        return getMinimumDelinquencyCycle(configAccountOptions?.collectionsStartNewPlanJourney)
    }

    override fun isViewTreatmentPlanSupported(): Boolean {
        return ((getAccountsDelinquencyCycle() ?: 0) >= (minimumViewTreatmentDelinquencyCycle()
            ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT)) && isTreatmentPlanSupported(
            configAccountOptions?.showTreatmentPlanJourney
        )
    }

    override fun isTakeUpTreatmentPlanJourneyEnabled(): Boolean {
        return ((getAccountsDelinquencyCycle() ?: 0) >= (minimumTakeUpTreatmentDelinquencyCycle()
            ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT)) && isTreatmentPlanSupported(
            configAccountOptions?.collectionsStartNewPlanJourney
        )
    }

    override fun productGroupCode(): String? = product?.productGroupCode

    override fun getAccountsDelinquencyCycle(): Int? = product?.delinquencyCycle

    private fun getMinimumDelinquencyCycle(treatmentPlan: ConfigShowTreatmentPlan?): Int? {
        return when (productGroupCode()) {
            productGroupCodeSc -> treatmentPlan?.storeCard?.minimumDelinquencyCycle
            productGroupCodePl -> treatmentPlan?.personalLoan?.minimumDelinquencyCycle
            else -> treatmentPlan?.creditCard?.minimumDelinquencyCycle
        }
    }

    private fun isTreatmentPlanSupported(treatmentPlan: ConfigShowTreatmentPlan?): Boolean {
        val appBuildNumber = Utils.getAppBuildNumber()
        return when (productGroupCode()) {
            productGroupCodeSc -> appBuildNumber >= (treatmentPlan?.personalLoan?.minimumSupportedAppBuildNumber
                ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT)
            productGroupCodePl -> appBuildNumber >= (treatmentPlan?.storeCard?.minimumSupportedAppBuildNumber
                ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT)
            else -> appBuildNumber >= (treatmentPlan?.creditCard?.minimumSupportedAppBuildNumber
                ?: MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT)
        }
    }

    companion object {
        const val MINIMUM_SUPPORTED_APP_BUILD_NUMBER_DEFAULT = 999
        val productGroupCodeSc: String = AccountsProductGroupCode.STORE_CARD.groupCode.lowercase()
        val productGroupCodePl: String = AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase()
    }
}