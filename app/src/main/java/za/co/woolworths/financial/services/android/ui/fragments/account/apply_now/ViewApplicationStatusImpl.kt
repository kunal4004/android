package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.app.Activity
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.ApplyNowLinks
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.util.KotlinUtils


class ViewApplicationStatusImpl(private val accountsResponse: AccountsResponse?) : ViewApplicationStatusInterface {

    override fun isViewApplicationStatusVisible(): Boolean = accountsResponse?.products?.size ?: 0 != 3

    override fun viewApplicationStatusLinkInExternalBrowser(section: MyAccountSection, activity: Activity?) {
        val applyNowLinkUrl = when(section){
            MyAccountSection.AccountLanding -> getApplyNowLink()?.applicationStatus
            MyAccountSection.CreditCardLanding -> getApplyNowLink()?.creditCard
            MyAccountSection.StoreCardLanding-> getApplyNowLink()?.storeCard
            MyAccountSection.PersonalLoanLanding -> getApplyNowLink()?.personalLoan
        }
        KotlinUtils.openUrlInPhoneBrowser(applyNowLinkUrl, activity)
    }

    override fun getApplyNowLink(): ApplyNowLinks? = WoolworthsApplication.getApplyNowLink()
}