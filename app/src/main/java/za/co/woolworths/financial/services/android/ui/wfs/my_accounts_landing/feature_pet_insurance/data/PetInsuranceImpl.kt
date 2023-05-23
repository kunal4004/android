package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.models.dto.app_config.account_options.PetInsuranceConfig
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.PET
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDResponse
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

typealias PetInsuranceUrlIsWebviewExitUrl = Triple<String, Boolean, String?>

interface  PetInsurance {
    fun isPetInsuranceEnabled(): Boolean
    fun isPetInsuranceIntroductionShown() : Boolean
    fun getPetInsuranceConfigFromMobileConfig(): PetInsuranceConfig?
    fun breakoutToWebViewParams(response: AppGUIDResponse?) : PetInsuranceUrlIsWebviewExitUrl
    fun isRenderModeWebView(config: PetInsuranceConfig?): Boolean
    fun getInsuranceProduct(productModel : PetInsuranceModel?) : InsuranceProducts?
}

class PetInsuranceImpl @Inject constructor() : PetInsurance {

    override fun isPetInsuranceEnabled(): Boolean {
        return Utils.isFeatureEnabled(AppConfigSingleton.accountOptions?.insuranceProducts?.minimumSupportedAppBuildNumber)
    }

    override fun isPetInsuranceIntroductionShown() = runBlocking {
        val isShown = withContext(Dispatchers.Default) {
            Utils.getSessionDaoValue(SessionDao.KEY.PET_INSURANCE_INTRODUCTION_SHOWED)
        }
        isShown.isNullOrEmpty()
    }

    override fun getPetInsuranceConfigFromMobileConfig(): PetInsuranceConfig? {
        return AppConfigSingleton.accountOptions?.insuranceProducts
    }

    override fun breakoutToWebViewParams(response: AppGUIDResponse?): PetInsuranceUrlIsWebviewExitUrl {
        val config = getPetInsuranceConfigFromMobileConfig()
        val petInsuranceUrl = config?.petInsuranceUrl + response?.appGuid
        val isRenderModeWebView = config?.renderMode == AvailableFundFragment.WEBVIEW
        val exitUrl = config?.exitUrl
        return PetInsuranceUrlIsWebviewExitUrl(petInsuranceUrl, isRenderModeWebView, exitUrl)
    }

    override fun isRenderModeWebView(config: PetInsuranceConfig?): Boolean {
        return config?.renderMode == AvailableFundFragment.WEBVIEW
    }

    override fun getInsuranceProduct(productModel: PetInsuranceModel?): InsuranceProducts? {
        return productModel?.insuranceProducts?.find { it.type == PET }
    }

}