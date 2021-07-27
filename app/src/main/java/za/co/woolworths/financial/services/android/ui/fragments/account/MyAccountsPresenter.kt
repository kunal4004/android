package za.co.woolworths.financial.services.android.ui.fragments.account

import za.co.woolworths.financial.services.android.ui.fragments.account.apply_now.ViewApplicationStatusInterface
import za.co.woolworths.financial.services.android.util.application.ApplicationInfoInterface

class MyAccountsPresenter(private val viewApplicationStatus: ViewApplicationStatusInterface,
private val applicationInfo: ApplicationInfoInterface) :
    ViewApplicationStatusInterface by viewApplicationStatus,
        ApplicationInfoInterface by applicationInfo

