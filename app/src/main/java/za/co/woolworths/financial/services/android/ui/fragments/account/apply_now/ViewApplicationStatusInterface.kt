package za.co.woolworths.financial.services.android.ui.fragments.account.apply_now

import android.app.Activity

interface ViewApplicationStatusInterface {
    fun isViewApplicationStatusVisible(): Boolean
    fun viewApplicationStatusLinkInExternalBrowser(activity: Activity?)
}