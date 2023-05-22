package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_application_status

import android.app.Activity
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigApplyNowLinks
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface ApplicationStatusView {
    fun isVisible(productSize: Int?): Boolean
    fun onClick(section: MyAccountSection, activity: Activity?)
    fun getApplyNowLink(): ConfigApplyNowLinks?
}

class ViewApplicationStatusImpl @Inject constructor() : ApplicationStatusView {

    override fun isVisible(productSize : Int?): Boolean = productSize != 3

    override fun onClick(section: MyAccountSection, activity: Activity?) {
        val applyNowLinkUrl = when (section) {
            MyAccountSection.AccountLanding -> getApplyNowLink()?.applicationStatus
            MyAccountSection.CreditCardLanding -> getApplyNowLink()?.creditCard
            MyAccountSection.StoreCardLanding -> getApplyNowLink()?.storeCard
            MyAccountSection.PersonalLoanLanding -> getApplyNowLink()?.personalLoan
        }
        KotlinUtils.openUrlInPhoneBrowser(applyNowLinkUrl, activity)
    }

    override fun getApplyNowLink(): ConfigApplyNowLinks? = AppConfigSingleton.applyNowLink

}