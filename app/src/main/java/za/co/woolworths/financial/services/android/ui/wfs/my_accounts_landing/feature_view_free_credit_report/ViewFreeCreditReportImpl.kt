package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_view_free_credit_report

import za.co.woolworths.financial.services.android.models.AppConfigSingleton.creditView
import javax.inject.Inject

interface FreeCreditReportView {
    fun isVisible(): Boolean
}

class ViewFreeCreditReportImpl @Inject constructor() : FreeCreditReportView {

    override fun isVisible(): Boolean {
        return creditView?.isEnabled ?: false
    }

}