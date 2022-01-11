package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.app.Activity
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigApplyNowLinks
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection

interface ViewApplicationStatusInterface {
    fun isViewApplicationStatusVisible(): Boolean
    fun viewApplicationStatusLinkInExternalBrowser(section: MyAccountSection = MyAccountSection.AccountLanding, activity: Activity?)
    fun getApplyNowLink(): ConfigApplyNowLinks?
}