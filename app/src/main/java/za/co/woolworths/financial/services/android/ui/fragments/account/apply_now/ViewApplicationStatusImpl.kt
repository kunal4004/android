package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.app.Activity
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ViewApplicationStatusImpl(private val accountsResponse: AccountsResponse?) : ViewApplicationStatusInterface {

    // TODO:: Replace by view appliction status url
    private val url: String = "https://www.google.com"

    override fun isViewApplicationStatusVisible(): Boolean =  accountsResponse?.products?.size ?: 0 != 3

    override fun viewApplicationStatusLinkInExternalBrowser(activity: Activity?) {
        KotlinUtils.openUrlInPhoneBrowser(url, activity)
    }

}